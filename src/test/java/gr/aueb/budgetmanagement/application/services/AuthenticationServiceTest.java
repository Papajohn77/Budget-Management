package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.application.commands.AuthenticateUserCommand;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.ports.PasswordHasher;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaUserRepository;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class AuthenticationServiceTest {
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test123!@#";

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private UserRepository userRepository;
    private AuthenticationService authenticationService;
    private PasswordHasher passwordHasher;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        userRepository = new JpaUserRepository(entityManager);
        passwordHasher = new BCryptPasswordEncoder();
        authenticationService = new AuthenticationService(userRepository, passwordHasher);

        createTestUser();
    }

    @AfterEach
    void tearDown() {
        if (transaction.isActive()) {
            transaction.rollback();
        }
        entityManager.close();
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

    @Test
    void testSuccessfulAuthentication() {
        AuthenticateUserCommand command = new AuthenticateUserCommand(
            TEST_EMAIL,
            TEST_PASSWORD
        );

        assertDoesNotThrow(() -> authenticationService.authenticate(command));
    }

    @Test
    void testAuthenticationWithNonexistentEmail() {
        AuthenticateUserCommand command = new AuthenticateUserCommand(
            "nonexistent@example.com",
            TEST_PASSWORD
        );

        assertThrows(
            InvalidCredentialsException.class,
            () -> authenticationService.authenticate(command)
        );
    }

    @Test
    void testAuthenticationWithWrongPassword() {
        AuthenticateUserCommand command = new AuthenticateUserCommand(
            TEST_EMAIL,
            "WrongPassword123!@#"
        );

        assertThrows(
            InvalidCredentialsException.class,
            () -> authenticationService.authenticate(command)
        );
    }
}
