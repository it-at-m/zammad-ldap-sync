package de.muenchen.mpdz.zammad.ldap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.muenchen.mpdz.zammad.ldap.service.ZammadLdapService;
import de.muenchen.mpdz.zammad.ldap.service.ZammadService;
import de.muenchen.mpdz.zammad.ldap.service.ZammadSyncService;
import de.muenchen.mpdz.zammad.ldap.tree.LdapOuNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

/**
 * https://www.baeldung.com/spring-rest-openapi-documentation
 * http://.../api-docs
 * http://.../swagger-ui/index.html
 *
 */

@RestController
@Slf4j
public class SyncController {

    @Autowired
    public ZammadService zammadService;

    @Autowired
    public ZammadLdapService ldapService;

    @Autowired
    public ZammadSyncService syncService;


    @Operation(summary="Synchronize ldap zammad ou and user.", description="Add/update zammad entities.\n"
    		+ "* timeStamp is only applied to users/employees of an organizational unit (ou) and not ou manager or ou itself.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/text") ),
                @ApiResponse(responseCode = "400", description = "BAD REQUEST" ),
            }
        )
    @PutMapping("/syncsubtree")
    public ResponseEntity<Object> synchronizeDnSubtree(@Schema(example="o=oubase,dc=example,dc=org") @RequestParam(required = true) String distinguishedName, @Schema(example="20240226083627Z (yyyyMMddHHmmssZ)") @RequestParam(required = false) String timeStamp) {
        try {
            var treeView = syncService.syncSubtreeByDn(distinguishedName, timeStamp);
            return new ResponseEntity<>(treeView, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }

   @Operation(summary="Flag user for deletion.", description="Check whether user has left the organization. Zammad automation job deletes user.\n * Be careful using this option. Every zammad user not found in DN subtree will be marked for deletion."
    		+ "Use only with same distinguished name (dn) like /syncsubtree (or as close at dn root as possible) to fetch all user you need in Zammad ! Do not use with dn selecting limited subtrees only !\n"
    		+ "* To delete users finally use Zammad automation with condition : user.ldapsync = \"delete\" (https://admin-docs.zammad.org/en/latest/manage/scheduler.html).\n"
            + "* Manually added users are ignored - unless they have an valid 'lhmobjectid' and are marked with 'donotupdate=false'.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/text") ),
                @ApiResponse(responseCode = "400", description = "BAD REQUEST" ),
            }
        )
    @DeleteMapping("/markusertodelete")
    public ResponseEntity<Object> flagUserToDeleteDnSubtree(@Schema(example="o=oubase,dc=example,dc=org") @RequestParam(required = true) String distinguishedName) {
        try {
            var treeView = syncService.markZammadUserToDelete(distinguishedName);
            return new ResponseEntity<>(treeView, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary="Get ldap subtree as json.", description="timeStamp is only applied to users/employees of an organizational unit (ou) and not ou manager or ou itself.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LdapOuNode.class)) ),
                @ApiResponse(responseCode = "400", description = "BAD REQUEST" ),
            }
        )
    @GetMapping("/subtreeasjson")
    public ResponseEntity<Object> subtreeAsJson(@Schema(example="o=oubase,dc=example,dc=org") @RequestParam(required = true) String distinguishedName, @Schema(example="20240226083627Z (yyyyMMddHHmmssZ)") @RequestParam(required = false) String timeStamp) {
        try {
            var treeView = syncService.subtreeAsJson(distinguishedName, timeStamp);
            return new ResponseEntity<>(treeView, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }


}
