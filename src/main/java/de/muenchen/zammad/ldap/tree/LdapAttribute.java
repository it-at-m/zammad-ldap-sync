package de.muenchen.zammad.ldap.tree;

public enum LdapAttribute {

    ORGANIZATIONAL_UNIT("organizationalUnit", "objectClass"),
    LHM_ORGANIZATIONAL_UNIT("lhmOrganizationalUnit", "objectClass"),
    GROUP("group", "objectClass");

    private final String identifier;
    private final String description;

    LdapAttribute(String identifier, String description) {
        this.identifier = identifier;
        this.description = description;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }

    public static LdapAttribute fromIdentifier(String identifier) {
        for (LdapAttribute attr : values()) {
            if (attr.getIdentifier().equalsIgnoreCase(identifier)) {
                return attr;
            }
        }
        throw new IllegalArgumentException("Unknown LDAP Attribute: " + identifier);
    }

}
