package de.muenchen.mpdz.zammad.ldap.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.muenchen.mpdz.zammad.ldap.domain.ZammadRoleDTO;
import de.muenchen.mpdz.zammad.ldap.service.ZammadLdapService;
import de.muenchen.mpdz.zammad.ldap.service.ZammadService;
import de.muenchen.mpdz.zammad.ldap.service.ZammadSyncService;
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

    @Operation(summary="Adds ldap ou to zammad role 'zuweisungsrolle'.", description="Set ldap ou authority in zammad.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "OK"),
                @ApiResponse(responseCode = "400", description = "BAD REQUEST" ),
            }
        )
    @PutMapping("/assignmentrole")
    public ResponseEntity<Object> updateAssignmentRole() {
        try {
            syncService.syncAssignmentRole();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary="Get assignment role.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ZammadRoleDTO.class)) ),
                @ApiResponse(responseCode = "400", description = "BAD REQUEST" ),
            }
        )
    @GetMapping("/assignmentrole")
    public ResponseEntity<ZammadRoleDTO> getAssignmentRole(@RequestParam(required = true) String id) {
        ZammadRoleDTO zammadRoleDTO = zammadService.getZammadRole(id);
        return new ResponseEntity<>(zammadRoleDTO, HttpStatus.OK);
    }

    @Operation(summary="Synchronize ldap zammad ou and user.", description="Add/update zammad entities.")
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

    @Operation(summary="Flag user for deletion.", description="Check whether user has left the organization. Zammad automation job delets user.")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/text") ),
                @ApiResponse(responseCode = "400", description = "BAD REQUEST" ),
            }
        )
    @DeleteMapping("/finduserdelete")
    public ResponseEntity<Object> flagUserToDeleteDnSubtree(@Schema(example="o=oubase,dc=example,dc=org") @RequestParam(required = true) String distinguishedName) {
        try {
            var treeView = syncService.flagZammadUserToDelete(distinguishedName);
            return new ResponseEntity<>(treeView, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }

//    @Operation(summary="Get ldap subtree as json")
//    @ApiResponses(
//            value = {
//                @ApiResponse(responseCode = "200", description = "OK", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LdapOuNode.class)) ),
//                @ApiResponse(responseCode = "400", description = "BAD REQUEST" ),
//            }
//        )
//    @GetMapping("/subtreeasjson")
//    public ResponseEntity<Object> subtreeAsJson(@Schema(example="o=oubase,dc=example,dc=org") @RequestParam(required = true) String distinguishedName, @Schema(example="20240226083627Z (yyyyMMddHHmmssZ)") @RequestParam(required = false) String timeStamp) {
//        try {
//            var treeView = syncService.subtreeAsJson(distinguishedName, timeStamp);
//            return new ResponseEntity<>(treeView, HttpStatus.OK);
//        } catch (Exception e) {
//            log.error(e.getLocalizedMessage());
//            return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
//        }
//    }


}
