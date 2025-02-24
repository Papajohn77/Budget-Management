package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.application.commands.AuthenticateUserCommand;
import gr.aueb.budgetmanagement.application.commands.RegisterUserCommand;
import gr.aueb.budgetmanagement.application.dto.RegisteredUserDTO;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.Savings;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.exceptions.EmailAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidEmailAddressException;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidPasswordException;
import gr.aueb.budgetmanagement.domain.exceptions.UsernameAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.ports.PasswordHasher;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaUserRepository;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class UserServiceTest {
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test123!@#";

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private UserRepository userRepository;
    private UserService userService;
    private PasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();
        
        userRepository = new JpaUserRepository(entityManager);
        passwordHasher = new BCryptPasswordEncoder();
        userService = new UserService(userRepository, passwordHasher);
    }

    @AfterEach
    void tearDown() {
        if (transaction.isActive()) {
            transaction.rollback();
        }
        entityManager.close();
    }

    @Test
    void testSuccessfulUserRegistration() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        RegisterUserCommand command = new RegisterUserCommand(
            username,
            email,
            TEST_PASSWORD
        );

        // Act
        RegisteredUserDTO result = userService.registerUser(command);

        // Assert
        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals("testuser", result.username());
        assertEquals("test@example.com", result.email());

        // Verify user was persisted
        assertTrue(userRepository.existsByUsername(username));
        assertTrue(userRepository.existsByEmail(email));

        // Verify Savings was created as part of user registration
        Long userId = entityManager.createQuery(
                "SELECT u.id FROM User u WHERE u.username = :username", 
                Long.class
            )
            .setParameter("username", username)
            .getSingleResult();

        Savings savings = entityManager.createQuery(
                "SELECT s FROM Savings s WHERE s.user.id = :userId",
                Savings.class
            )
            .setParameter("userId", userId)
            .getSingleResult();

        assertNotNull(savings);
        assertEquals(userId, savings.getUser().getId());
    }

    @Test
    void testDuplicateUsername() {
        // Arrange
        RegisterUserCommand firstUser = new RegisterUserCommand(
            "testuser",
            "test1@example.com",
            TEST_PASSWORD
        );
        RegisterUserCommand duplicateUsername = new RegisterUserCommand(
            "testuser",
            "test2@example.com",
            TEST_PASSWORD
        );

        // Act & Assert
        assertDoesNotThrow(
            () -> userService.registerUser(firstUser)
        );
        assertThrows(
            UsernameAlreadyExistsException.class, 
            () -> userService.registerUser(duplicateUsername))
        ;
    }

    @Test
    void testDuplicateEmail() {
        // Arrange
        RegisterUserCommand firstUser = new RegisterUserCommand(
            "user1",
            "test@example.com",
            TEST_PASSWORD
        );
        RegisterUserCommand duplicateEmail = new RegisterUserCommand(
            "user2",
            "test@example.com",
            TEST_PASSWORD
        );

        // Act & Assert
        assertDoesNotThrow(
            () -> userService.registerUser(firstUser)
        );
        assertThrows(
            EmailAlreadyExistsException.class, 
            () -> userService.registerUser(duplicateEmail)
        );
    }

    @Test
    void testInvalidEmail() {
        // Arrange
        RegisterUserCommand invalidEmail = new RegisterUserCommand(
            "testuser",
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
    void testInvalidPassword() {
        // Arrange
        RegisterUserCommand weakPassword = new RegisterUserCommand(
            "testuser",
            "test@example.com",
            "weak"
        );

        // Act & Assert
        assertThrows(
            InvalidPasswordException.class, 
            () -> userService.registerUser(weakPassword)
        );
    }

    @Test
    void testPasswordEncryption() {
        // Arrange
        String username = "testuser";
        String email = "test@example.com";
        String rawPassword = TEST_PASSWORD;
        RegisterUserCommand command = new RegisterUserCommand(
            username,
            email,
            rawPassword
        );

        // Act
        RegisteredUserDTO result = userService.registerUser(command);

        // Assert
        String storedPassword = entityManager.createQuery(
                "SELECT u.password FROM User u WHERE u.id = :id",
                String.class
            )
            .setParameter("id", result.id())
            .getSingleResult();

        assertTrue(passwordHasher.verifyPassword(rawPassword, storedPassword));
    }

    @Test
    void testSuccessfulAuthentication() {
        createTestUser();

        AuthenticateUserCommand command = new AuthenticateUserCommand(
            TEST_EMAIL,
            TEST_PASSWORD
        );

        assertDoesNotThrow(() -> userService.authenticate(command));
    }

    @Test
    void testAuthenticationWithNonexistentEmail() {
        createTestUser();

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
    void testAuthenticationWithWrongPassword() {
        createTestUser();

        AuthenticateUserCommand command = new AuthenticateUserCommand(
            TEST_EMAIL,
            "WrongPassword123!@#"
        );

        assertThrows(
            InvalidCredentialsException.class,
            () -> userService.authenticate(command)
        );
    }

    private void createTestUser() {
        User user = User.create(
            TEST_USERNAME,
            TEST_EMAIL,
            TEST_PASSWORD,
            passwordHasher
        );
        entityManager.persist(user);
    }
}
