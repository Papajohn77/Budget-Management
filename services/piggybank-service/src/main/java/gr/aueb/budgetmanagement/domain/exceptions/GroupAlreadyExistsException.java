package gr.aueb.budgetmanagement.domain.exceptions;

public class GroupAlreadyExistsException extends AlreadyExistsDomainException {
    public GroupAlreadyExistsException(String message) {
        super(message);
    }
}
