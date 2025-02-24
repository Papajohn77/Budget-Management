package gr.aueb.budgetmanagement.infrastructure.security;

import org.mindrot.jbcrypt.BCrypt;

import gr.aueb.budgetmanagement.domain.ports.PasswordHasher;

public class BCryptPasswordEncoder implements PasswordHasher {
    private final int logRounds;

    public BCryptPasswordEncoder() {
        this.logRounds = 12;
    }

    @Override
    public String hashPassword(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean verifyPassword(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }
}
