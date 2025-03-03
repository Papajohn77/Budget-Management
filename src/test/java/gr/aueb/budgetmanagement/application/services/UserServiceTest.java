package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.commands.AuthenticateUserCommand;
import gr.aueb.budgetmanagement.application.commands.RegisterUserCommand;
import gr.aueb.budgetmanagement.application.exceptions.AlreadyExistsException;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.RegisteredUserRepresentation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidEmailAddressException;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidPasswordException;
import gr.aueb.budgetmanagement.domain.ports.PasswordHasher;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class UserServiceTest {
    private static final String TEST_USERNAME = "othertestuser";
    private static final String TEST_EMAIL = "other@example.com";
    private static final String TEST_PASSWORD = "Test123!@#";

    @Inject
    private UserRepository userRepository;

    @Inject
    private UserService userService;

    @Inject
    private PasswordHasher passwordHasher;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
    }

    @Test
    @TestTransaction
    void testSuccessfulUserRegistration() {
        // Arrange
        String username = TEST_USERNAME;
        String email = TEST_EMAIL;
        RegisterUserCommand command = new RegisterUserCommand(
            username,
            email,
            TEST_PASSWORD
        );

        // Act
        RegisteredUserRepresentation result = userService.registerUser(command);

        // Assert
        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(username, result.username());
        assertEquals(email, result.email());

        // Verify user was persisted
        assertTrue(userRepository.existsByUsername(username));
        assertTrue(userRepository.existsByEmail(email));

        // Verify Savings was created as part of user registration
        User registeredUser = userRepository.findById(result.id()).orElseThrow();
        assertNotNull(registeredUser.getSavings());
    }

    @Test
    @TestTransaction
    void testDuplicateUsername() {
        // Arrange
        RegisterUserCommand duplicateUsername = new RegisterUserCommand(
            user.getUsername(), 
            TEST_EMAIL,
            TEST_PASSWORD
        );

        // Act & Assert
        assertThrows(
            AlreadyExistsException.class, 
            () -> userService.registerUser(duplicateUsername))
        ;
    }

    @Test
    @TestTransaction
    void testDuplicateEmail() {
        // Arrange
        RegisterUserCommand duplicateEmail = new RegisterUserCommand(
            TEST_USERNAME,
            user.getEmail().getValue(), 
            TEST_PASSWORD
        );

        // Act & Assert
        assertThrows(
                AlreadyExistsException.class, 
            () -> userService.registerUser(duplicateEmail)
        );
    }

    @Test
    @TestTransaction
    void testInvalidEmail() {
        // Arrange
        RegisterUserCommand invalidEmail = new RegisterUserCommand(
            TEST_USERNAME,
            "invalid-email",
            TEST_PASSWORD
        );

        // Act & Assert
        assertThrows(
            InvalidEmailAddressException.class, 
            () -> userService.registerUser(invalidEmail)
        );
    }

    @Test
    @TestTransaction
    void testInvalidPassword() {
        // Arrange
        RegisterUserCommand weakPassword = new RegisterUserCommand(
            TEST_USERNAME,
            TEST_EMAIL,
            "weak"
        );

        // Act & Assert
        assertThrows(
            InvalidPasswordException.class, 
            () -> userService.registerUser(weakPassword)
        );
    }

    @Test
    @TestTransaction
    void testPasswordEncryption() {
        // Arrange
        RegisterUserCommand command = new RegisterUserCommand(
            TEST_USERNAME,
            TEST_EMAIL,
            TEST_PASSWORD
        );

        // Act
        RegisteredUserRepresentation result = userService.registerUser(command);

        // Assert
        User registeredUser = userRepository.findById(result.id()).orElseThrow();
        assertTrue(registeredUser.verifyPassword(TEST_PASSWORD, passwordHasher));
    }

    @Test
    @TestTransaction
    void testSuccessfulAuthentication() {
        AuthenticateUserCommand command = new AuthenticateUserCommand(
            user.getEmail().getValue(),
            TEST_PASSWORD
        );

        assertDoesNotThrow(() -> userService.authenticate(command));
    }

    @Test
    @TestTransaction
    void testAuthenticationWithNonexistentEmail() {
        AuthenticateUserCommand command = new AuthenticateUserCommand(
            "nonexistent@example.com",
            TEST_PASSWORD
        );

        assertThrows(
            InvalidCredentialsException.class,
            () -> userService.authenticate(command)
        );
    }

    @Test
    @TestTransaction
    void testAuthenticationWithWrongPassword() {
        AuthenticateUserCommand command = new AuthenticateUserCommand(
            user.getEmail().getValue(),
            "WrongPassword123!@#"
        );

        assertThrows(
            InvalidCredentialsException.class,
            () -> userService.authenticate(command)
        );
    }
}
