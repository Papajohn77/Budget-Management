package gr.aueb.budgetmanagement.domain.exceptions;

public class InvitationAlreadyRespondedToException extends AlreadyExistsDomainException {
    public InvitationAlreadyRespondedToException(String message) {
        super(message);
    }
}
