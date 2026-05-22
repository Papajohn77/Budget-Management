package gr.aueb.budgetmanagement.infrastructure.persistence.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidEmailAddressException;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;

class EmailAddressConverterTest {
    private final EmailAddressConverter converter = new EmailAddressConverter();

    @Test
    void testConvertToDatabaseColumn() {
        EmailAddress email = new EmailAddress("test@example.com");
        String dbValue = converter.convertToDatabaseColumn(email);
        assertEquals("test@example.com", dbValue);
    }

    @Test
    void testConvertNullToDatabaseColumn() {
        String dbValue = converter.convertToDatabaseColumn(null);
        assertNull(dbValue);
    }

    @Test
    void testConvertToEntityAttribute() {
        String dbValue = "test@example.com";
        EmailAddress email = converter.convertToEntityAttribute(dbValue);
        assertEquals(dbValue, email.getValue());
    }

    // Should never happen in practice since email column is not nullable
    @Test
    void testConvertInvalidEmailToEntityAttribute() {
        String invalidEmail = "invalid-email";
        assertThrows(
            InvalidEmailAddressException.class, 
            () -> converter.convertToEntityAttribute(invalidEmail)
        );
    }
}
