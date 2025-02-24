package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.application.commands.RegisterUserCommand;
import gr.aueb.budgetmanagement.application.dto.RegisteredUserDTO;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.Savings;
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

class UserRegistrationServiceTest {
    private static final String TEST_PASSWORD = "Test123!@#";

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private UserRepository userRepository;
    private UserRegistrationService userRegistrationService;
    private PasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();
        
        userRepository = new JpaUserRepository(entityManager);
        passwordHasher = new BCryptPasswordEncoder();
        userRegistrationService = new UserRegistrationService(userRepository, passwordHasher);
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
        RegisteredUserDTO result = userRegistrationService.registerUser(command);

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
            () -> userRegistrationService.registerUser(firstUser)
        );
        assertThrows(
            UsernameAlreadyExistsException.class, 
            () -> userRegistrationService.registerUser(duplicateUsername))
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
            () -> userRegistrationService.registerUser(firstUser)
        );
        assertThrows(
            EmailAlreadyExistsException.class, 
            () -> userRegistrationService.registerUser(duplicateEmail)
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
            () -> userRegistrationService.registerUser(invalidEmail)
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
            () -> userRegistrationService.registerUser(weakPassword)
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
        RegisteredUserDTO result = userRegistrationService.registerUser(command);

        // Assert
        String storedPassword = entityManager.createQuery(
                "SELECT u.password FROM User u WHERE u.id = :id",
                String.class
            )
            .setParameter("id", result.id())
            .getSingleResult();

        assertTrue(passwordHasher.verifyPassword(rawPassword, storedPassword));
    }
}
