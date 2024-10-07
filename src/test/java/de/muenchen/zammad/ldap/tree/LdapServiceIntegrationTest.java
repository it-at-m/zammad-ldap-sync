/*
 * The MIT License
 * Copyright © 2023 Landeshauptstadt München | it@M
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.muenchen.zammad.ldap.tree;

import de.muenchen.oss.ezldap.core.EnhancedLdapOuAttributesMapper;
import de.muenchen.oss.ezldap.core.EnhancedLdapUserAttributesMapper;
import de.muenchen.oss.ezldap.core.LdapBaseUserAttributesMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

@Testcontainers
class LdapServiceIntegrationTest {

    private LdapService ldapService;

    private static final int OPENLDAP_EXPOSED_PORT = 389;
    private static final String USER_BASE = "o=users,dc=example,dc=org";
    private static final String ORG_BASE = "o=oubase,dc=example,dc=org";
    private static final String LDAP_DOMAIN = "example.org";

    @Container
    private static final GenericContainer<?> openldapContainer = new GenericContainer<>("osixia/openldap:1.5.0")
            .withNetworkAliases("openldap")
            .withCommand("--copy-service --loglevel debug")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("openldap")))
            .withEnv("LDAP_ORGANISATION", "Example Inc.")
            .withEnv("LDAP_DOMAIN", LDAP_DOMAIN)
            .withExposedPorts(OPENLDAP_EXPOSED_PORT)
            .waitingFor(Wait.forLogMessage(".*\\/container\\/run\\/process\\/slapd\\/run started as PID.*\\n", 1))
            .withFileSystemBind(MountableFile.forClasspathResource("/ldap/schema/lhm.schema").getResolvedPath(),
                    "/container/service/slapd/assets/config/bootstrap/schema/lhm.schema",
                    BindMode.READ_ONLY)
            .withFileSystemBind(MountableFile.forClasspathResource("/ldap/data").getResolvedPath(),
                    "/container/service/slapd/assets/config/bootstrap/ldif/custom",
                    BindMode.READ_ONLY);

    private LdapTemplate ldapTemplate(final LdapContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }

    private LdapContextSource contextSource(final int port) {
        final LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUrl("ldap://localhost:" + port);
        ldapContextSource.setUserDn("cn=admin,dc=example,dc=org");
        ldapContextSource.setPassword("admin");
        ldapContextSource.afterPropertiesSet();
        return ldapContextSource;
    }

    @BeforeEach
    void beforeEach() {
        Integer exposedPort = openldapContainer.getMappedPort(OPENLDAP_EXPOSED_PORT);
        System.out.println(exposedPort);
        final LdapContextSource contextSource = this.contextSource(exposedPort);
        final LdapBaseUserAttributesMapper baseUserAttributesMapper = new LdapBaseUserAttributesMapper();
        this.ldapService = new LdapService(this.ldapTemplate(contextSource),
                new EnhancedLdapUserAttributesMapper(baseUserAttributesMapper),
                baseUserAttributesMapper, new EnhancedLdapOuAttributesMapper(), new DtoMapperImpl(), USER_BASE,
                ORG_BASE);
    }


    @Test
    void calculate_shade_tree() {

        var shadetree = this.ldapService.buildSubtree("o=oubase,dc=example,dc=org", null);
        Assertions.assertTrue(shadetree.isPresent());
        var rootNode = shadetree.get().values().iterator().next();
        Assertions.assertEquals("o=oubase,dc=example,dc=org", rootNode.getDistinguishedName());
        Assertions.assertEquals("342", rootNode.getNode().getLhmObjectId());

        var rbs = rootNode.getChildNodes().values().iterator().next();
        Assertions.assertEquals("Referat für Bildung und Sport", rbs.getNode().getOu());

        var departments = rbs.getChildNodes();
        var abt_1 = departments.get("Abteilung 1");
        Assertions.assertEquals("ou=Abteilung 1,ou=Referat für Bildung und Sport,o=oubase,dc=example,dc=org", abt_1.getDistinguishedName());
        Assertions.assertEquals("ou=Abteilung 1,ou=Referat für Bildung und Sport,o=oubase,dc=example,dc=org", abt_1.getUsers().get(0).getLhmObjectPath());

    }
    
    @Test
    void calculate_shade_tree_with_parent_node_path() {

        var shadetree = this.ldapService.buildSubtree("ou=Abteilung 1,ou=Referat für Bildung und Sport,o=oubase,dc=example,dc=org", null);
        Assertions.assertTrue(shadetree.isPresent());
        var rootNode = shadetree.get().values().iterator().next();
        Assertions.assertEquals("o=oubase,dc=example,dc=org", rootNode.getDistinguishedName());
        Assertions.assertEquals("342", rootNode.getNode().getLhmObjectId());

        var rbs = rootNode.getChildNodes().values().iterator().next();
        Assertions.assertEquals("Referat für Bildung und Sport", rbs.getNode().getOu());

        var departments = rbs.getChildNodes();
        var abt_1 = departments.get("ou=Abteilung 1,ou=Referat für Bildung und Sport,o=oubase,dc=example,dc=org");
        Assertions.assertEquals("ou=Abteilung 1,ou=Referat für Bildung und Sport,o=oubase,dc=example,dc=org", abt_1.getDistinguishedName());
        Assertions.assertEquals("ou=Abteilung 1,ou=Referat für Bildung und Sport,o=oubase,dc=example,dc=org", abt_1.getUsers().get(0).getLhmObjectPath());

    }
    
    

    @Test
    void calculate_shade_tree_select_user_with_modifyTimestamp() {

        var shadetree = this.ldapService.buildSubtree("o=oubase,dc=example,dc=org", "20240226083627Z");
        Assertions.assertTrue(shadetree.isPresent());
        Assertions.assertEquals(1, shadetree.get().size());
        var rootNode = shadetree.get().values().iterator().next();
        Assertions.assertNotNull(rootNode.getNode().getModifyTimeStamp(), "Operational ldap ou attribute modifyTimestamp not selected.");
        var rbs = rootNode.getChildNodes().values().iterator().next();
        Assertions.assertEquals(1, rbs.getUsers().size(), "User expected. All users were created after the timestamp");
        Assertions.assertNotNull(rbs.getUsers().get(0).getModifyTimeStamp(), "Operational ldap user attribute modifyTimestamp not selected.");

        shadetree = this.ldapService.buildSubtree("o=oubase,dc=example,dc=org", "30000000000000Z");
        Assertions.assertTrue(shadetree.isPresent());
        Assertions.assertEquals(1, shadetree.get().size());
        rootNode = shadetree.get().values().iterator().next();
        rbs = rootNode.getChildNodes().values().iterator().next();
        Assertions.assertEquals(0, rbs.getUsers().size(), "No user expected. The timestamp is too far in the future.");

    }

    @Test
    void shade_tree_override_toString() {

        var shadetree = this.ldapService.buildSubtree("o=oubase,dc=example,dc=org", null);
        Assertions.assertTrue(shadetree.isPresent());
        var rootNode = shadetree.get().values().iterator().next();

        var tree = rootNode.toString();
        Assertions.assertTrue(tree.contains("***** New LDAP entry : RBS-A-2 Abteilung 2 *****"));

    }

    @Test
    void shade_tree_flat_list_user() {

        var shadetree = this.ldapService.buildSubtree("o=oubase,dc=example,dc=org", null);
        Assertions.assertTrue(shadetree.isPresent());
        var rootNode = shadetree.get().values().iterator().next();

        var users = rootNode.flatListLdapUserDTO();
        Assertions.assertEquals(3, users.size());
    }

    @Test
    void shade_tree_flat_list_ou() {

        var shadetree = this.ldapService.buildSubtree("o=oubase,dc=example,dc=org", null);
        Assertions.assertTrue(shadetree.isPresent());
        var rootNode = shadetree.get().values().iterator().next();

        var lhmobjectids = rootNode.flatListLdapOuDTO();
        Assertions.assertEquals(4, lhmobjectids.size());
    }

}
