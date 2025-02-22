package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;

class GroupTest {
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "hashedPassword123";

    private User admin;

    @BeforeEach
    void setUp() {
        admin = User.create(
            TEST_USERNAME,
            new EmailAddress(TEST_EMAIL),
            TEST_PASSWORD
        );
    }

    @Test
    void createWithValidData() {
        Group group = Group.create("Test Group", admin);

        assertEquals("Test Group", group.getName());
        assertEquals(admin, group.getAdmin());
        assertTrue(group.getMembers().contains(admin));
        assertEquals(1, group.getMembers().size());
    }

    @Test
    void createWithNullName() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> Group.create(null, admin)
        );
    }

    @Test
    void createWithBlankName() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> Group.create("   ", admin)
        );
    }

    @Test
    void createWithNullAdmin() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> Group.create("Test Group", null)
        );
    }

    @Test
    void addMemberWithValidUserShouldAddToMembers() {
        Group group = Group.create("Test Group", admin);
        User newMember = createNonAdminUser();

        group.addMember(newMember);

        assertTrue(group.getMembers().contains(newMember));
        assertEquals(2, group.getMembers().size());
    }

    @Test
    void addMemberWithNullUserShouldThrowException() {
        Group group = Group.create("Test Group", admin);

        assertThrows(
            InvalidDomainArgumentException.class,
            () -> group.addMember(null)
        );
    }

    @Test
    void addMember_WhenMemberAlreadyExists_ShouldNotDuplicate() {
        Group group = Group.create("Test Group", admin);

        group.addMember(admin);

        assertEquals(1, group.getMembers().size());
    }

    @Test
    void getMembersShouldReturnUnmodifiableSet() {
        Group group = Group.create("Test Group", admin);
        Set<User> members = group.getMembers();

        assertThrows(
            UnsupportedOperationException.class,
            () -> members.add(new User())
        );
    }

    private User createNonAdminUser() {
        return User.create(
            "nonadmin",
            new EmailAddress("nonadmin@example.com"),
            TEST_PASSWORD
        );
    }
}
