package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.AuthenticateUserCommand;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.ports.PasswordHasher;
import jakarta.validation.Valid;

public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public AuthenticationService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public void authenticate(@Valid AuthenticateUserCommand command) {
        User user = userRepository.findByEmail(command.email())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!user.verifyPassword(command.password(), passwordHasher)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
    }
}
