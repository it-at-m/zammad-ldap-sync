# Zammad-Ldap-Synchronsiation
In order to assign an owner to Zammad tickets for processing, we need a structured representation of our LDAP entries in Zammad. 

Zammad tickets holders must be mapped to our LDAP users.

The project enables the transfer of our LDAP *organizational units* and *users* to Zammad *groups* and *users*.

Changes in LDAP must be able to be periodically synchronized in Zammad.

### Built With

* Spring boot
  * REST
  * LDAP / [ezLDAP](https://github.com/it-at-m/ezLDAP)
* Openapi
* [Zammad REST API](https://docs.zammad.org/en/latest/api/intro.html)

## Roadmap

*TODO...*

See the [open issues](#) for a full list of proposed features (and known issues).


## Set up
For the LDAP-Zammad Synchronization we create an LDAP tree consisting of the organizational units and their employees. We call this a *shade tree*.
To do this, the data must be read from LDAP. The implementation fits our LDAP structure. If the LDAP structure is different, appropriate adjustments are required, to build the shade tree.

The shade tree is then used to synchronize the groups and users in Zammd via REST API.

Prepare Zammad before first start. Technical documentation can be found here [docs/Readme](https://github.com/it-at-m/zammad-ldap-sync/blob/dev/docs/README.md)

application.yaml:
- Connect zammad-ldap-snyc with Zammad REST-API (Url, Token).
- Connect zammad-ldap-snyc with your LDAP.

Start Zammad-Ldap-Synchronisation application.

Request Openapi documentation http(s)://[url:port]/swagger-ui/index.html.

Start synchronisation process with correct rest ressource.

## Documentation
Why don't we use the [Zammad LDAP](https://admin-docs.zammad.org/en/latest/system/integrations/ldap/index.html#limitations) connection ? The main reasons are: 
- Mapping / Synchronizing organizations is not possible
- Nested groups are not supported.
- The ticket owners can be found more quickly using nested groups.
- When a user originates from an LDAP server, Zammad will try to verify the login credentials against LDAP first. No Single-Sign-On (SSO) integration is possible.

More technical documentation can be found here [docs/Readme](https://github.com/it-at-m/zammad-ldap-sync/blob/dev/docs/README.md)

## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please open an issue with the tag "enhancement", fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Open an issue with the tag "enhancement"
2. Fork the Project
3. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
4. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
5. Push to the Branch (`git push origin feature/AmazingFeature`)
6. Open a Pull Request

More about this in the [CODE_OF_CONDUCT](/CODE_OF_CONDUCT.md) file.


## License

Distributed under the MIT License. See [LICENSE](LICENSE) file for more information.


## Contact

it@M - opensource@muenchen.de
