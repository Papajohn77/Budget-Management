package gr.aueb.budgetmanagement.domain.exceptions;

public class SavingsAlreadyExistsException extends InvalidDomainArgumentException {
    public SavingsAlreadyExistsException(String message) {
        super(message);
    }
}
