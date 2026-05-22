package gr.aueb.budgetmanagement.domain.exceptions;

public class InvalidMoneyException extends InvalidDomainArgumentException {
    public InvalidMoneyException(String message) {
        super(message);
    }
}
