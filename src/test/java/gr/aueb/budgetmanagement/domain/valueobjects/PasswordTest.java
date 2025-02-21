package gr.aueb.budgetmanagement.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidPasswordException;

class PasswordTest {
    @Test
    void testValidPassword() {
        String validPassword = "Test123!@#";
        Password password = new Password(validPassword);
        assertEquals(validPassword, password.getValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "", 
        "   ", 
        "short1!", 
        "nouppercase123!", 
        "NOLOWERCASE123!", 
        "NoSpecialChar123", 
        "NoNumber!@#abc"
    })
    void testInvalidPasswords(String invalidPassword) {
        assertThrows(
            InvalidPasswordException.class, 
            () -> new Password(invalidPassword)
        );
    }

    @Test
    void testPasswordMasking() {
        Password password = new Password("Test123!@#");
        assertEquals("********", password.toString());
    }
}
