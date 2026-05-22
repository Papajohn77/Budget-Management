package gr.aueb.budgetmanagement.domain.exceptions;

public class AlreadyExistsDomainException extends RuntimeException {
    public AlreadyExistsDomainException(String message) {
        super(message);
    }
}
