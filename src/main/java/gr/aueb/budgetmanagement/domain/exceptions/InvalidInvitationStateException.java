package gr.aueb.budgetmanagement.domain.exceptions;

public class InvalidInvitationStateException extends InvalidDomainArgumentException {
    public InvalidInvitationStateException(String message) {
        super(message);
    }

}
