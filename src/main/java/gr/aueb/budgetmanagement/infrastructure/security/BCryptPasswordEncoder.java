package gr.aueb.budgetmanagement.infrastructure.security;

import org.mindrot.jbcrypt.BCrypt;

import gr.aueb.budgetmanagement.application.ports.PasswordEncoder;

public class BCryptPasswordEncoder implements PasswordEncoder {
    private final int logRounds;

    public BCryptPasswordEncoder() {
        this.logRounds = 12;
    }

    @Override
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return BCrypt.checkpw(rawPassword, hashedPassword);
    }
}
