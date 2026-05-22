package gr.aueb.budgetmanagement.infrastructure.persistence.converters;

import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmailAddressConverter implements AttributeConverter<EmailAddress, String> {
    @Override
    public String convertToDatabaseColumn(EmailAddress email) {
        return email != null ? email.getValue() : null;
    }

    @Override
    public EmailAddress convertToEntityAttribute(String dbEmail) {
        return new EmailAddress(dbEmail);
    }
}
