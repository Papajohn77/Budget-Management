package gr.aueb.budgetmanagement.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidEmailAddressException;

class EmailAddressTest {

    @ParameterizedTest
    @ValueSource(strings = {
        "test@example.com",
        "user.name@domain.com",
        "user+label@example.com",
        "firstname.lastname@subdomain.domain.com",
        "user@domain-name.com",
        "123@domain.com"
    })
    void constructor_WithValidEmails(String validEmail) {
        assertDoesNotThrow(() -> new EmailAddress(validEmail));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "invalid",
        "@domain.com",
        "test@",
        "test@domain",
        "test@.com",
        "test..test@domain.com",
        "test@domain..com",
        "test@domain.com.",
        ".test@domain.com",
        "test@domain.c", // TLD too short
        "test@domain.commission" // TLD too long
    })
    void constructor_WithInvalidEmails(String invalidEmail) {
        assertThrows(
            InvalidEmailAddressException.class,
            () -> new EmailAddress(invalidEmail)
        );
    }

    @Test
    void constructor_WithNullEmail_ShouldThrowException() {
        assertThrows(
            InvalidEmailAddressException.class,
            () -> new EmailAddress(null)
        );
    }

    @Test
    void equals_hashCode_WithSameEmail() {
        String email = "test@example.com";
        EmailAddress email1 = new EmailAddress(email);
        EmailAddress email2 = new EmailAddress(email);

        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    void equals_hashCode_WithDifferentEmail() {
        EmailAddress email1 = new EmailAddress("test1@example.com");
        EmailAddress email2 = new EmailAddress("test2@example.com");

        assertNotEquals(email1, email2);
        assertNotEquals(email1.hashCode(), email2.hashCode());
    }
}
