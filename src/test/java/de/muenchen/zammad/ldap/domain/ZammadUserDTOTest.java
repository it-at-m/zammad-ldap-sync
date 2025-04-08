package de.muenchen.zammad.ldap.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import de.muenchen.zammad.domain.User;
import lombok.val;

class ZammadUserDTOTest {

    @Test
    void testEqualsAndHashCode() {
        val userA = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        var userB = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        assertTrue(userA.equals(userB), "Objects are not equal unexpectedly");
        assertEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are not equal unexpectedly");
    }

    @Test
    void testEqualsAndHashCode_Id() {
        val userA = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        var userB = new User("newId", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        assertTrue(userA.equals(userB), "Objects are not equal unexpectedly");
        assertEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are not equal unexpectedly");
    }
    @Test
    void testEqualsAndHashCode_Firstname() {
        val userA = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        var userB = new User("id", "Jane", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        assertFalse(userA.equals(userB), "Objects are equal unexpectedly");
        assertNotEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");
    }

    @Test
    void testEqualsAndHashCode_Lastname() {
        val userA = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        var userB = new User("id", "John", "Smith", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        assertFalse(userA.equals(userB), "Objects are equal unexpectedly");
        assertNotEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");
    }

    @Test
    void testEqualsAndHashCode_Login() {
        val userA = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        var userB = new User("id", "John", "Doe", "jane.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        assertTrue(userA.equals(userB), "Objects are not equal unexpectedly");
        assertEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are not equal unexpectedly");
    }

    @Test
    void testEqualsAndHashCode_Ldapsyncupdate() {
        val userA = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        var userB = new User("id", "John", "Doe", "john.doe", false, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        assertTrue(userA.equals(userB), "Objects are not equal unexpectedly");
        assertEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are not equal unexpectedly");
    }

    @Test
    void testEqualsAndHashCode_Email() {
        val userA = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        var userB = new User("id", "John", "Doe", "john.doe", true, "jane.smith@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        assertFalse(userA.equals(userB), "Objects are equal unexpectedly");
        assertNotEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");
    }

    @Test
    void testEqualsAndHashCode_Department() {   // Test changing department
        val userA = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        var userB = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "HR", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        assertFalse(userA.equals(userB), "Objects are equal unexpectedly");
        assertNotEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");
    }

    @Test
    void testEqualsAndHashCode_LhmObjectId() {
        val userA = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        var userB = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "789012", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        assertFalse(userA.equals(userB), "Objects are equal unexpectedly");
        assertNotEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");
    }

    @Test
    void testEqualsAndHashCode_RoleIds() {
        val userA = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        var userB = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(1, 2, 3), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        assertFalse(userA.equals(userB), "Objects are equal unexpectedly");
        assertNotEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");
    }

    @Test
    void testEqualsAndHashCode_GroupIds() {
        val userA = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        var userB = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of("group1", List.of("member1")), "1970-01-01T00:00:00Z", false, "unsynced");
        assertFalse(userA.equals(userB), "Objects are equal unexpectedly");
        assertNotEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");
    }

    @Test
    void testEqualsAndHashCode_UpdatedAt() {
        val userA = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        var userB = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "2023-01-01T00:00:00Z", false, "unsynced");
        assertTrue(userA.equals(userB), "Objects are not equal unexpectedly");
        assertEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are not equal unexpectedly");
    }

    @Test
    void testEqualsAndHashCode_Active() {
        val userA = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        var userB = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", true, "unsynced");
        assertTrue(userA.equals(userB), "Objects are not equal unexpectedly");
        assertEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");
    }
    @Test
    void testEqualsAndHashCode_LdapSyncState() {
        val userA = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "unsynced");
        var userB = new User("id", "John", "Doe", "john.doe", true, "john.doe@example.com", "IT", "123456", List.of(), Map.of(), "1970-01-01T00:00:00Z", false, "synced");
        assertTrue(userA.equals(userB), "Objects are not equal unexpectedly");
        assertEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");
    }

    @Test
    void testEqualsGroupIds() {
        // Setup
        User dto1 = new User(null, null, null, null, false, null, null, null, null, Map.of("group1", List.of("member1", "member2"), "group2", List.of("member3")), null, false, null);
        User dto2 = new User(null, null, null, null, false, null, null, null, null, Map.of("group1", List.of("member1", "member2"), "group2", List.of("member3")), null, false, null);
        User dto3 = new User(null, null, null, null, false, null, null, null, null, Map.of("group1", List.of("member1", "member2"), "group2", List.of("member4")), null, false, null);
        User dto4 = new User(null, null, null, null, false, null, null, null, null, Map.of(), null, false, null);
        User dto5 = new User(null, null, null, null, false, null, null, null, null, null, null, false, null);

        // Test
        assertTrue(dto1.equals(dto2), "Objects are not equal unexpectedly"); // Same structure
        assertFalse(dto1.equals(dto3), "Objects are equal unexpectedly"); // Different value in one group
        assertFalse(dto1.equals(dto4), "Objects are equal unexpectedly"); // Empty map
        assertFalse(dto1.equals(dto5), "Objects are equal unexpectedly"); // Null map
    }

}