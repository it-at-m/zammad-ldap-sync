package de.muenchen.zammad.ldap.domain;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ZammadUserDTOTest {

    @Test
    void testEqualsAndHashCode() {
        // Create base DTOs
        val userA = createZammadUserDTO();
        var userB = createZammadUserDTO();

        // Test with default values
        assertEquals(userA, userB, "Objects are not equal");
        assertEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are not equal");

        // Test changing id (should not affect equals/hashcode)
        userB.setId("newId");
        assertEquals(userA, userB, "Objects are not equal unexpectedly");
        assertEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are not equal unexpectedly");

        // Test changing firstname
        userB = createZammadUserDTO();
        userB.setFirstname("Jane");
        assertNotEquals(userA, userB, "Objects are equal unexpectedly");
        assertNotEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");

        // Test changing lastname
        userB = createZammadUserDTO();
        userB.setLastname("Smith");
        assertNotEquals(userA, userB, "Objects are equal unexpectedly");
        assertNotEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");

        // Test changing login (should not affect equals/hashcode)
        userB = createZammadUserDTO();
        userB.setLogin("jane.doe");
        assertEquals(userA, userB, "Objects are equal unexpectedly");
        assertEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");

        // Test changing ldapsyncupdate (should not affect equals/hashcode)
        userB = createZammadUserDTO();
        userB.setLdapsyncupdate(false);
        assertEquals(userA, userB, "Objects are equal unexpectedly");
        assertEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");

        // Test changing email
        userB = createZammadUserDTO();
        userB.setEmail("jane.smith@example.com");
        assertNotEquals(userA, userB, "Objects are equal unexpectedly");
        assertNotEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");

        // Test changing department
        userB = createZammadUserDTO();
        userB.setDepartment("HR");
        assertNotEquals(userA, userB, "Objects are equal unexpectedly");
        assertNotEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");

        // Test changing lhmobjectid
        userB = createZammadUserDTO();
        userB.setLhmobjectid("789012");
        assertNotEquals(userA, userB, "Objects are equal unexpectedly");
        assertNotEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");

        // Test changing roleIds (should not affect equals/hashcode)
        userB = createZammadUserDTO();
        userB.setRoleIds(List.of(1, 2, 3));
        assertEquals(userA, userB, "Objects are equal unexpectedly");
        assertEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");

        // Test changing groupIds
        userB = createZammadUserDTO();
        userB.setGroupIds(Map.of("group1", List.of("member1")));
        assertNotEquals(userA, userB, "Objects are equal unexpectedly");
        assertNotEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");

        // Test changing updatedAt (should not affect equals/hashcode)
        userB = createZammadUserDTO();
        userB.setUpdatedAt("2023-01-01T00:00:00Z");
        assertEquals(userA, userB, "Objects are equal unexpectedly");
        assertEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");

        // Test changing active status (should not affect equals/hashcode)
        userB = createZammadUserDTO();
        userB.setActive(true);
        assertEquals(userA, userB, "Objects are equal unexpectedly");
        assertEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");

        // Test changing ldapsyncstate (should not affect equals/hashcode)
        userB = createZammadUserDTO();
        userB.setLdapsyncstate("synced");
        assertEquals(userA, userB, "Objects are equal unexpectedly");
        assertEquals(userA.hashCode(), userB.hashCode(), "Hashcodes are equal unexpectedly");
    }

    private ZammadUserDTO createZammadUserDTO() {
        val userDto = new ZammadUserDTO();
        userDto.setId("id");
        userDto.setLogin("john.doe");
        userDto.setLdapsyncupdate(true);
        userDto.setFirstname("John");
        userDto.setLastname("Doe");
        userDto.setEmail("john.doe@example.com");
        userDto.setDepartment("IT");
        userDto.setLhmobjectid("123456");
        userDto.setRoleIds(List.of());
        userDto.setGroupIds(Map.of());
        userDto.setUpdatedAt("1970-01-01T00:00:00Z");
        userDto.setActive(false);
        userDto.setLdapsyncstate("unsynced");
        return userDto;
    }


    @Test
    void testEqualsGroupIds() {
        // Setup
        ZammadUserDTO dto1 = new ZammadUserDTO();
        dto1.setGroupIds(Map.of(
                "group1", List.of("member1", "member2"),
                "group2", List.of("member3")
        ));

        ZammadUserDTO dto2 = new ZammadUserDTO();
        dto2.setGroupIds(Map.of(
                "group1", List.of("member1", "member2"),
                "group2", List.of("member3")
        ));

        ZammadUserDTO dto3 = new ZammadUserDTO();
        dto3.setGroupIds(Map.of(
                "group1", List.of("member1", "member2"),
                "group2", List.of("member4") // Different value
        ));

        ZammadUserDTO dto4 = new ZammadUserDTO();
        dto4.setGroupIds(Map.of());

        ZammadUserDTO dto5 = new ZammadUserDTO();
        dto5.setGroupIds(null);

        // Test
        assertEquals(dto1,dto2); // Same structure
        assertNotEquals(dto1,dto3); // Different value in one group
        assertNotEquals(dto1,dto4); // Empty map
        assertNotEquals(dto1,dto5); // Null map

        // Additional test to check if order matters
        ZammadUserDTO dto6 = new ZammadUserDTO();
        dto6.setGroupIds(Map.of(
                "group2", List.of("member3"),
                "group1", List.of("member1", "member2")
        ));

        assertEquals(dto1,dto6); // Order matters
    }

}