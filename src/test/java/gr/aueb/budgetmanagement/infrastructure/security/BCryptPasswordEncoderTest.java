package gr.aueb.budgetmanagement.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BCryptPasswordEncoderTest {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Test
    void testPasswordEncoding() {
        String rawPassword = "Test123!@#";
        String encodedPassword = encoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encodedPassword.startsWith("$2a$"));
    }

    @Test
    void testPasswordMatching() {
        String rawPassword = "Test123!@#";
        String encodedPassword = encoder.encode(rawPassword);

        assertTrue(encoder.matches(rawPassword, encodedPassword));
        assertFalse(encoder.matches("WrongPassword123!@#", encodedPassword));
    }

    @Test
    void testDifferentHashesForSamePassword() {
        String password = "Test123!@#";
        String firstHash = encoder.encode(password);
        String secondHash = encoder.encode(password);

        assertNotEquals(firstHash, secondHash);
        assertTrue(encoder.matches(password, firstHash));
        assertTrue(encoder.matches(password, secondHash));
    }
}
