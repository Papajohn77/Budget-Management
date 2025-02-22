package gr.aueb.budgetmanagement.domain.exceptions;

public class InvalidEmailAddressException extends InvalidDomainArgumentException {
    public InvalidEmailAddressException(String message) {
        super(message);
    }
}
