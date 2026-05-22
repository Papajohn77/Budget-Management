package gr.aueb.budgetmanagement.domain.exceptions;

public class SavingsAlreadyExistsException extends AlreadyExistsDomainException {
    public SavingsAlreadyExistsException(String message) {
        super(message);
    }
}
