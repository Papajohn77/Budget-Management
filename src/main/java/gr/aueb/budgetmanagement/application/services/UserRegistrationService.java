package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.RegisterUserCommand;
import gr.aueb.budgetmanagement.application.dto.RegisteredUserDTO;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.exceptions.EmailAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.exceptions.UsernameAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.ports.PasswordHasher;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

public class UserRegistrationService {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserRegistrationService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public RegisteredUserDTO registerUser(@Valid RegisterUserCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new UsernameAlreadyExistsException("Username already exists: " + command.username());
        }

        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException("Email already exists: " + command.email());
        }

        User user = User.create(
            command.username(),
            command.email(),
            command.password(),
            passwordHasher
        );

        userRepository.save(user);

        return new RegisteredUserDTO(
            user.getId(), 
            user.getUsername(), 
            user.getEmail().getValue()
        );
    }
}
