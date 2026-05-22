package gr.aueb.budgetmanagement.domain.exceptions;

public class InsufficientSavingsException extends InvalidDomainArgumentException {
    public InsufficientSavingsException(String message) {
        super(message);
    }
}
