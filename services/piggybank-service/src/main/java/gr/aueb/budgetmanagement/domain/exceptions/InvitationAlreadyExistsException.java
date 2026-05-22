package gr.aueb.budgetmanagement.domain.exceptions;

public class InvitationAlreadyExistsException extends AlreadyExistsDomainException {
    public InvitationAlreadyExistsException(String message) {
        super(message);
    }
}
