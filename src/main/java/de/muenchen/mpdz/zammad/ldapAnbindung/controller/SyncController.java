package de.muenchen.mpdz.zammad.ldapAnbindung.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.muenchen.mpdz.zammad.ldapAnbindung.domain.ZammadRoleDTO;
import de.muenchen.mpdz.zammad.ldapAnbindung.service.ZammadLdapService;
import de.muenchen.mpdz.zammad.ldapAnbindung.service.ZammadService;
import de.muenchen.mpdz.zammad.ldapAnbindung.service.ZammadSyncService;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class SyncController {

    @Autowired
    public ZammadService zammadService;

    @Autowired
    public ZammadLdapService ldapService;

    @Autowired
    public ZammadSyncService syncService;

    @GetMapping("/updateassignmentrole")
    public ResponseEntity<Object> updateAssignmentRole() {
        try {
            syncService.syncAssignmentRole();
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/getassignmentrole")
    public ResponseEntity<ZammadRoleDTO> getAssignmentRole(@RequestParam (defaultValue = "4") String id) {
        ZammadRoleDTO zammadRoleDTO = zammadService.getZammadRole(id);
        return new ResponseEntity<>(zammadRoleDTO, HttpStatus.OK);
    }

    @GetMapping("/syncsubtree")
    public ResponseEntity<Object> synchronizeDnSubtree(@RequestParam(required = true) String distinguishedName, @RequestParam(required = false) String timeStamp, @RequestParam(required = false) Boolean deleteUser) {
        try {
            var fullsyncWithDelete = deleteUser != null ? deleteUser : false;
            var treeView = syncService.syncSubtreeByDn(distinguishedName, timeStamp, fullsyncWithDelete);
            return new ResponseEntity<>(treeView, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return new ResponseEntity<>(e.getLocalizedMessage(), HttpStatus.BAD_REQUEST);
        }
    }

}
