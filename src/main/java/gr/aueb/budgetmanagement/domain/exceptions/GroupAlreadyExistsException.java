package gr.aueb.budgetmanagement.domain.exceptions;

public class GroupAlreadyExistsException extends InvalidDomainArgumentException {
    public GroupAlreadyExistsException(String message) {
        super(message);
    }
}
