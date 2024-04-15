## Customize this file after creating the new REPO and remove this lines.
What to adjust:  
* Add the your project or repo name direct under the logo.
* Add a short and long desciption.
* Add links for your final repo to report a bug or request a feature.
* Add list of used technologies.
* If you have, add a roadmap or remove this section.
* Fill up the section for set up and documentation.
 * Start in this file only with documentation and link to the docs folder.
* Add project shields. Use [shields.io](https://shields.io/)

## ------- end to remove -------
<!-- add Project Logo, if existing -->

# repo or project name

*Add a description from your project here.*


### Built With

The documentation project is built with technologies we use in our projects:

* Spring boot
  * REST
  * LDAP / [ezLDAP](https://github.com/it-at-m/ezLDAP)
* Openapi
* [Zammad REST API](https://docs.zammad.org/en/latest/api/intro.html)

## Roadmap

*if you have a ROADMAP for your project add this here*


See the [open issues](#) for a full list of proposed features (and known issues).


## Set up
For the LDAP-Zammad Synchronization we create an LDAP tree consisting of the organizational units and their employees.
To do this, the data must be read from LDAP. The implementation fits our LDAP structure. If the LDAP structure is different, appropriate adjustments are required, to build the tree.

The LDAP tree is then used to synchronize the organizational units hierarchy including employees in Zammd via REST API.

Prepare Zammad before first start. Technical documentation can be found here [docs/Readme](https://github.com/it-at-m/zammad-ldap-sync/blob/dev/docs/README.md)

application.yaml:
- Connect zammad-ldap-snyc with Zammad REST-API (Url, Token).
- Connect zammad-ldap-snyc with your LDAP.

Start Zammad-Ldap synchronisation.

Request Openapi documentation http(s)://[url:port]/swagger-ui/index.html.

Start synchronisation with correct rest ressource.

## Documentation
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
