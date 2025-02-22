package gr.aueb.budgetmanagement.domain.exceptions;

public class InvalidDomainArgumentException extends RuntimeException {
    public InvalidDomainArgumentException(String message) {
        super(message);
    }
}
