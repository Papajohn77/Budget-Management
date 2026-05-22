package gr.aueb.budgetmanagement.domain.valueobjects;

import org.apache.commons.validator.routines.EmailValidator;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidEmailAddressException;

public class EmailAddress {
    private final String value;

    public EmailAddress(String value) {
        if (value == null || !isValidEmail(value)) {
            throw new InvalidEmailAddressException("Invalid email address: " + value);
        }
        this.value = value;
    }

    private boolean isValidEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        EmailAddress email = (EmailAddress) o;
        return value.equals(email.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
