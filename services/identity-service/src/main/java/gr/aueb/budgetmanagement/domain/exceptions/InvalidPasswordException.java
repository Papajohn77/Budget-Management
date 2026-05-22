package gr.aueb.budgetmanagement.domain.exceptions;

public class InvalidPasswordException extends InvalidDomainArgumentException {
    public InvalidPasswordException(String message) {
        super(message);
    }
}
