package gr.aueb.budgetmanagement.domain.exceptions;

public class InviteeAlreadyInGroupException extends AlreadyExistsDomainException {
    public InviteeAlreadyInGroupException(String message) {
        super(message);
    }
}
