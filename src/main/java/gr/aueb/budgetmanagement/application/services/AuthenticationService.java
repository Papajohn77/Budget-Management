package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.AuthenticateUserCommand;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.ports.PasswordEncoder;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import jakarta.validation.Valid;

public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void authenticate(@Valid AuthenticateUserCommand command) {
        EmailAddress email = new EmailAddress(command.email());
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }
    }
}
