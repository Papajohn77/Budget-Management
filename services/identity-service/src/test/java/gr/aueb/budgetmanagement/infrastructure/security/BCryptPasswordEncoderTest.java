package gr.aueb.budgetmanagement.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BCryptPasswordEncoderTest {
    private static final String TEST_PASSWORD = "Test123!@#";

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void testPasswordEncoding() {
        String rawPassword = TEST_PASSWORD;
        String encodedPassword = encoder.hashPassword(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$"));
    }

    @Test
    void testPasswordMatching() {
        String rawPassword = TEST_PASSWORD;
        String encodedPassword = encoder.hashPassword(rawPassword);

        assertTrue(encoder.verifyPassword(rawPassword, encodedPassword));
        assertFalse(encoder.verifyPassword("WrongPassword123!@#", encodedPassword));
    }

    @Test
    void testDifferentHashesForSamePassword() {
        String password = TEST_PASSWORD;
        String firstHash = encoder.hashPassword(password);
        String secondHash = encoder.hashPassword(password);

        assertNotEquals(firstHash, secondHash);
        assertTrue(encoder.verifyPassword(password, firstHash));
        assertTrue(encoder.verifyPassword(password, secondHash));
    }
}
