package gr.aueb.budgetmanagement.domain.exceptions;

public class InvitationAlreadyExistsException extends InvalidDomainArgumentException {
    public InvitationAlreadyExistsException(String message) {
        super(message);
    }
}
