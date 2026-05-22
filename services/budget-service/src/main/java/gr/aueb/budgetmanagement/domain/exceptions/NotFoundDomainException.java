package gr.aueb.budgetmanagement.domain.exceptions;

public class NotFoundDomainException extends RuntimeException {
    public NotFoundDomainException(String message) {
        super(message);
    }
}
