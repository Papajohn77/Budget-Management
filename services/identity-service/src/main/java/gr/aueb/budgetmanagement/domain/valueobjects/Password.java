package gr.aueb.budgetmanagement.domain.valueobjects;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidPasswordException;

public class Password {
    private final String value;

    public Password(String value) {
        validatePassword(value);
        this.value = value;
    }

    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new InvalidPasswordException("Password cannot be empty");
        }

        if (password.length() < 8) {
            throw new InvalidPasswordException("Password must be at least 8 characters long");
        }

        if (!containsUpperCase(password)) {
            throw new InvalidPasswordException("Password must contain at least one uppercase letter");
        }

        if (!containsLowerCase(password)) {
            throw new InvalidPasswordException("Password must contain at least one lowercase letter");
        }

        if (!containsDigit(password)) {
            throw new InvalidPasswordException("Password must contain at least one digit");
        }

        if (!containsSpecialCharacter(password)) {
            throw new InvalidPasswordException("Password must contain at least one special character");
        }
    }

    private boolean containsUpperCase(String password) {
        return password.matches(".*[A-Z].*");
    }

    private boolean containsLowerCase(String password) {
        return password.matches(".*[a-z].*");
    }

    private boolean containsDigit(String password) {
        return password.matches(".*\\d.*");
    }

    private boolean containsSpecialCharacter(String password) {
        return password.matches(".*[!@#$%^&*()\\-+=<>?].*");
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "********";
    }
}
