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
- for `Benutzer`:
    - create Attribut:
        - NAME: `lhmobjectid`
        - ANZEIGE: `LHM Object ID`
        - FORMAT: `Textfeld`
    - create Attribut:
        - NAME: `ldapsyncupate`
        - ANZEIGE: `Ldap Synchronisation anwenden`
        - FORMAT: `Boolean-Feld`
        - STANDARD: `true`
  - create Attribut:
      - NAME: `ldapsyncstate`
      - ANZEIGE: `LDAP Synchronisation (delete etc.)`
      - FORMAT: `Textfeld`

- for `Gruppen`:
    - create Attribut:
        - NAME: `lhmobjectid_group`
        - ANZEIGE: `LHM Object ID`
        - FORMAT: `Textfeld`
    - create Attribut:
        - NAME: `ldapsyncupate_group`
        - ANZEIGE: `Ldap Synchronisation anwenden`
        - FORMAT: `Boolean-Feld`
        - STANDARD: `true`

- Restart Zammad to apply the changes.

In `Einstellungen --> Rollen`:
- create new Role:
    - NAME: `Erstellen`
    - Check: `ticket->agent`

- create new Role:
    - NAME: `Vollzugriff`
    - Check: `ticket->agent`

Update role id 'Erstellen' and 'Vollzugriff' in application.yaml
 ```
zammad:
  assignment:
    role:
      id-agent: 2
      id-erstellen: 4
      id-vollzugriff: 5
 ```
# Configuration properties
Where possible, the **application.yaml** is pre-filled.
However, some properties still need to be added.
```
ldap:
  url: [ldap url]
  userSearchBase: [ldap user search base]
  ouSearchBase: [ldap organizational unit search base]

zammad:
  token: Token token= [Zammad user profil generated access token]
  url:
    base: [Zammad REST API url]
```
