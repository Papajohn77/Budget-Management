package gr.aueb.budgetmanagement.domain.exceptions;

public class UsernameAlreadyExistsException extends InvalidDomainArgumentException {
    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
