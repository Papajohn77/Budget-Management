package gr.aueb.budgetmanagement.domain.exceptions;

public class EmailAlreadyExistsException extends InvalidDomainArgumentException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
