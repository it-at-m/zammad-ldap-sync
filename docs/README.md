# LDAP synchronisation with Zammad
This is a component to synchronize LDAP organizational units (ou) and user into Zammad groups and user.
Because group hierarchies can be created in Zammad it is possible to migrate our LDAP ou hierarchy into Zammad and assign LDAP user to the Zammad groups.
We call the hierarchy a _shadow tree_.

The component builds a LDAP shadow tree starting form LDAP distinguished name (DN) and follows the branching to the leaves.

It is possible to start near the leaves and gradually move towards the roots. The Zammad group hierarchy is expanded accordingly.

It is possible to delete user, but currently Zammad does not allow to delete groups. Deleting users is also not possible directly via REST API. We first mark the users and then delete them using Zammad automation.

## Zammad configuration via the interface
The synchronisation needs some adjustments in the Zammad database structure. It can be customized via Zammad interface or REST API.

### Interface
In `Einstellungen --> Objekte`:
- for `Benutzer` und `Gruppen`:
    - create Attribut:
        - NAME: `lhmobjectid`
        - ANZEIGE: `LHM Object ID`
        - FORMAT: `Textfeld`
    - create Attribut:
        - NAME: `donotupdate`
        - ANZEIGE: `Do Not Update (aus LDAP)`
        - FORMAT: `Boolean-Feld`
        - STANDARD: `false`
  - create Attribut:
      - NAME: `ldapsync`
      - ANZEIGE: `LDAP Synchronisation (delete etc.)`
      - FORMAT: `Textfeld`
- Restart Zammad to apply the changes. 

In `Einstellungen --> Rollen`:
- create new Role:
    - NAME: `Zuweisungsrolle`
    - Check: `ticket->agent`
