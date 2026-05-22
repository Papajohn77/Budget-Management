package gr.aueb.budgetmanagement.domain.valueobjects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void constructorWithValidEmails(String validEmail) {
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
    void constructorWithInvalidEmails(String invalidEmail) {
        assertThrows(
            InvalidEmailAddressException.class,
            () -> new EmailAddress(invalidEmail)
        );
    }

    @Test
    void constructorWithNullEmail() {
        assertThrows(
            InvalidEmailAddressException.class,
            () -> new EmailAddress(null)
        );
    }

    @Test
    void equalsWithNullObject() {
        EmailAddress email = new EmailAddress("test@example.com");

        assertNotEquals(email, null);
    }

    @Test
    void equalsWithDifferentClassObject() {
        String email = "test@example.com";
        EmailAddress emailAddress = new EmailAddress("test@example.com");

        assertNotEquals(emailAddress, email);
    }

    @Test
    void equalsAndHashCodeWithSameEmailObject() {
        EmailAddress email = new EmailAddress("test@example.com");

        assertEquals(email, email);
        assertEquals(email.hashCode(), email.hashCode());
    }

    @Test
    void equalsAndHashCodeWithSameEmail() {
        String email = "test@example.com";
        EmailAddress email1 = new EmailAddress(email);
        EmailAddress email2 = new EmailAddress(email);

        assertEquals(email1, email2);
        assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    void equalsAndHashCodeWithDifferentEmail() {
        EmailAddress email1 = new EmailAddress("test1@example.com");
        EmailAddress email2 = new EmailAddress("test2@example.com");

        assertNotEquals(email1, email2);
        assertNotEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    void testToString() {
        String email = "test@example.com";
        EmailAddress emailAddress = new EmailAddress(email);
        assertEquals(email, emailAddress.toString());
    }
}
