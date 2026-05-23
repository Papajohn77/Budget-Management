package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;

class UserTest {
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test123!@#";

    private User user;

    @BeforeEach
    void setUp() {
        user = User.create(
            TEST_USERNAME,
            TEST_EMAIL,
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );
    }

    @Test
    void createWithNullUsername() {
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> User.create(null, TEST_EMAIL, TEST_PASSWORD, new BCryptPasswordEncoder())
        );
    }

    @Test
    void createWithEmptyUsername() {
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> User.create("   ", TEST_EMAIL, TEST_PASSWORD, new BCryptPasswordEncoder())
        );
    }

    @Test
    void equalsWithNullObject() {
        assertNotEquals(user, null);
    }

    @Test
    void equalsAndHashCode_equalUsers() {
        User user1 = User.create(
            TEST_USERNAME,
            TEST_EMAIL,
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );

        User user2 = User.create(
            TEST_USERNAME,
            TEST_EMAIL,
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void equalsAndHashCode_notEqualUsers() {
        User user1 = User.create(
            TEST_USERNAME,
            TEST_EMAIL,
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );

        User user2 = User.create(
            "validusername",
            "validemail@example.com",
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );

        assertNotEquals(user1, user2);
        assertNotEquals(user1.hashCode(), user2.hashCode());
    }
}
