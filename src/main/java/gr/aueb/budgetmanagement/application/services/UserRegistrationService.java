package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.RegisterUserCommand;
import gr.aueb.budgetmanagement.application.dto.RegisteredUserDTO;
import gr.aueb.budgetmanagement.application.ports.PasswordEncoder;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.exceptions.EmailAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.exceptions.UsernameAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.domain.valueobjects.Password;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

public class UserRegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisteredUserDTO registerUser(@Valid RegisterUserCommand command) {
        EmailAddress email = new EmailAddress(command.email());
        Password password = new Password(command.password());

        if (userRepository.existsByUsername(command.username())) {
            throw new UsernameAlreadyExistsException("Username already exists: " + command.username());
        }

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already exists: " + command.email());
        }

        User user = User.create(
            command.username(),
            email,
            passwordEncoder.encode(password.getValue())
        );

        userRepository.save(user);

        return new RegisteredUserDTO(
            user.getId(), 
            user.getUsername(), 
            user.getEmail().getValue()
        );
    }
}
