package gr.aueb.budgetmanagement.domain.exceptions;

public class InvalidEmailAddressException extends RuntimeException {
    public InvalidEmailAddressException(String message) {
        super(message);
    }
}
