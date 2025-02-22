package gr.aueb.budgetmanagement.domain.exceptions;

public class InvalidMoneyException extends RuntimeException {
    public InvalidMoneyException(String message) {
        super(message);
    }
}
