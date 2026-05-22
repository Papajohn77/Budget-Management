package gr.aueb.budgetmanagement.domain.ports;

public interface PasswordHasher {
    String hashPassword(String rawPassword);
    boolean verifyPassword(String rawPassword, String hashedPassword);
}
