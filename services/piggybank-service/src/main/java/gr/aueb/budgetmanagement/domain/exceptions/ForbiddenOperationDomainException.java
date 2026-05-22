package gr.aueb.budgetmanagement.domain.exceptions;

public class ForbiddenOperationDomainException extends RuntimeException {
    public ForbiddenOperationDomainException(String message) {
        super(message);
    }
}
