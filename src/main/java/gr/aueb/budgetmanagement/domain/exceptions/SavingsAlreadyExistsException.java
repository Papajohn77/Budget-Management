package gr.aueb.budgetmanagement.domain.exceptions;

public class SavingsAlreadyExistsException extends RuntimeException {
    public SavingsAlreadyExistsException(String message) {
        super(message);
    }
}
