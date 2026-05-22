package gr.aueb.budgetmanagement.application.services;

import gr.aueb.budgetmanagement.application.commands.AuthenticateUserCommand;
import gr.aueb.budgetmanagement.application.commands.RegisterUserCommand;
import gr.aueb.budgetmanagement.application.exceptions.AlreadyExistsException;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.AccessTokenRepresentation;
import gr.aueb.budgetmanagement.application.representations.BalanceRepresentation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.ports.PasswordHasher;
import gr.aueb.budgetmanagement.infrastructure.security.JwtTokenService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@ApplicationScoped
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    private final PasswordHasher passwordHasher;

    public UserService(
        UserRepository userRepository, 
        JwtTokenService jwtTokenService,
        PasswordHasher passwordHasher
    ) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
        this.passwordHasher = passwordHasher;
    }

    @Transactional
    public AccessTokenRepresentation registerUser(@Valid RegisterUserCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new AlreadyExistsException("Username already exists: " + command.username());
        }

        if (userRepository.existsByEmail(command.email())) {
            throw new AlreadyExistsException("Email already exists: " + command.email());
        }

        User user = User.create(
            command.username(),
            command.email(),
            command.password(),
            passwordHasher
        );

        userRepository.save(user);

        String accessToken = jwtTokenService.generateToken(user);

        return new AccessTokenRepresentation("Bearer", accessToken);
    }

    @Transactional
    public AccessTokenRepresentation authenticateUser(@Valid AuthenticateUserCommand command) {
        User user = userRepository.findByEmail(command.email())
            .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        if (!user.verifyPassword(command.password(), passwordHasher)) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        String accessToken = jwtTokenService.generateToken(user);

        return new AccessTokenRepresentation("Bearer", accessToken);
    }

    @Transactional
    public BalanceRepresentation getBalance(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        return new BalanceRepresentation(user.getCurrentBalance());
    }
}
