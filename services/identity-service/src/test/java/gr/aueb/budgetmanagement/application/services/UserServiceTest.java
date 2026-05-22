package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.commands.AuthenticateUserCommand;
import gr.aueb.budgetmanagement.application.commands.RegisterUserCommand;
import gr.aueb.budgetmanagement.application.exceptions.AlreadyExistsException;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.AccessTokenRepresentation;
import gr.aueb.budgetmanagement.application.representations.BalanceRepresentation;
import gr.aueb.budgetmanagement.domain.entities.PersonalPiggyBank;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidEmailAddressException;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidPasswordException;
import gr.aueb.budgetmanagement.domain.ports.PasswordHasher;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
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
    void testSuccessfulUserRegistration() throws JsonProcessingException {
        // Arrange
        String username = TEST_USERNAME;
        String email = TEST_EMAIL;
        RegisterUserCommand command = new RegisterUserCommand(
            username,
            email,
            TEST_PASSWORD
        );

        // Act
        AccessTokenRepresentation result = userService.registerUser(command);

        // Assert
        assertNotNull(result);
        assertNotNull(result.accessToken());
        assertEquals("Bearer", result.tokenType());

        // Assert - Verify token is properly formatted
        String token = result.accessToken();
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts");
        
        // Decode and verify essential claims
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode claims = mapper.readTree(payload);
        
        // Verify essential user claims
        assertEquals(TEST_EMAIL, claims.get("sub").asText());
        assertTrue(claims.has("user_id"));
        assertNotNull(claims.get("exp"));

        // Verify user was persisted
        assertTrue(userRepository.existsByUsername(username));
        assertTrue(userRepository.existsByEmail(email));

        // Verify Savings was created as part of user registration
        User registeredUser = userRepository.findByEmail(email).orElseThrow();
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
        userService.registerUser(command);

        // Assert
        User registeredUser = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
        assertTrue(registeredUser.verifyPassword(TEST_PASSWORD, passwordHasher));
    }

    @Test
    @TestTransaction
    void testSuccessfulAuthentication() throws JsonProcessingException {
        AuthenticateUserCommand command = new AuthenticateUserCommand(
            user.getEmail().getValue(),
            TEST_PASSWORD
        );

        // Act
        AccessTokenRepresentation result = userService.authenticateUser(command);

        // Assert
        assertNotNull(result);
        assertNotNull(result.accessToken());
        assertEquals("Bearer", result.tokenType());

        // Assert - Verify token is properly formatted
        String token = result.accessToken();
        String[] parts = token.split("\\.");
        assertEquals(3, parts.length, "JWT should have 3 parts");

        // Decode and verify essential claims
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode claims = mapper.readTree(payload);

        // Verify essential user claims
        assertEquals(user.getEmail().getValue(), claims.get("sub").asText());
        assertTrue(claims.has("user_id"));
        assertNotNull(claims.get("exp"));
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
            () -> userService.authenticateUser(command)
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
            () -> userService.authenticateUser(command)
        );
    }

    @Test
    @TestTransaction
    void testGetBalanceForNewUser() {
        // Arrange
        RegisterUserCommand command = new RegisterUserCommand(
            TEST_USERNAME,
            TEST_EMAIL,
            TEST_PASSWORD
        );
        userService.registerUser(command);
        User newUser = userRepository.findByEmail(TEST_EMAIL).orElseThrow();

        // Act
        BalanceRepresentation balance = userService.getBalance(newUser.getId());

        // Assert
        assertNotNull(balance);
        assertEquals(BigDecimal.ZERO, balance.balance());
    }

    @Test
    @TestTransaction
    void testGetBalanceWithIncome() {
        // Arrange
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();

        BalanceRepresentation initialBalance = userService.getBalance(user.getId());
        Money incomeAmount = new Money(new BigDecimal("500.00"));
        user.addIncome(incomeAmount, LocalDate.now(), IncomeCategory.SALARY);
        userRepository.save(user);

        // Act
        BalanceRepresentation updatedBalance = userService.getBalance(user.getId());

        // Assert
        assertEquals(
            initialBalance.balance().add(incomeAmount.getValue()),
            updatedBalance.balance()
        );
    }

    @Test
    @TestTransaction
    void testGetBalanceWithExpense() {
        // Arrange
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();

        BalanceRepresentation initialBalance = userService.getBalance(user.getId());
        Money expenseAmount = new Money(new BigDecimal("200.00"));
        user.addExpense(expenseAmount, LocalDate.now(), ExpenseCategory.FOOD);
        userRepository.save(user);

        // Act
        BalanceRepresentation updatedBalance = userService.getBalance(user.getId());

        // Assert
        assertEquals(
            initialBalance.balance().subtract(expenseAmount.getValue()),
            updatedBalance.balance()
        );
    }

    @Test
    @TestTransaction
    void testGetBalanceWithSavingsAllocation() {
        // Arrange
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();

        BalanceRepresentation initialBalance = userService.getBalance(user.getId());
        Money savingsAmount = new Money(new BigDecimal("150.00"));
        user.allocateSavings(savingsAmount, LocalDate.now());
        userRepository.save(user);

        // Act
        BalanceRepresentation updatedBalance = userService.getBalance(user.getId());

        // Assert
        assertEquals(
            initialBalance.balance().subtract(savingsAmount.getValue()),
            updatedBalance.balance()
        );
    }

    @Test
    @TestTransaction
    void testGetBalanceWithPiggyBankAllocation() {
        // Arrange
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();

        BalanceRepresentation initialBalance = userService.getBalance(user.getId());

        PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
            "Test Piggy Bank",
            new Money(new BigDecimal("500.00")),
            ExpenseCategory.ENTERTAINMENT,
            user
        );

        Money allocationAmount = new Money(new BigDecimal("100.00"));
        piggyBank.allocate(allocationAmount, LocalDate.now(), user);
        userRepository.save(user);
        
        // Act
        BalanceRepresentation updatedBalance = userService.getBalance(user.getId());
        
        // Assert
        assertEquals(
            initialBalance.balance().subtract(allocationAmount.getValue()),
            updatedBalance.balance()
        );
    }

    @Test
    @TestTransaction
    void testGetBalanceWithMultipleTransactions() {
        // Arrange
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();

        BalanceRepresentation initialBalance = userService.getBalance(user.getId());

        PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
            "Test Piggy Bank",
            new Money(new BigDecimal("500.00")),
            ExpenseCategory.ENTERTAINMENT,
            user
        );

        Money incomeAmount = new Money(new BigDecimal("1000.00"));
        user.addIncome(incomeAmount, LocalDate.now(), IncomeCategory.SALARY);

        Money expenseAmount = new Money(new BigDecimal("300.00"));
        user.addExpense(expenseAmount, LocalDate.now(), ExpenseCategory.FOOD);

        Money savingsAmount = new Money(new BigDecimal("200.00"));
        user.allocateSavings(savingsAmount, LocalDate.now());

        Money piggyBankAmount = new Money(new BigDecimal("100.00"));
        piggyBank.allocate(piggyBankAmount, LocalDate.now(), user);
        
        userRepository.save(user);
        
        // Act
        BalanceRepresentation updatedBalance = userService.getBalance(user.getId());
        
        // Assert - Calculate expected change in balance
        BigDecimal expectedChange = incomeAmount.getValue()
            .subtract(expenseAmount.getValue())
            .subtract(savingsAmount.getValue())
            .subtract(piggyBankAmount.getValue());
            
        assertEquals(
            initialBalance.balance().add(expectedChange),
            updatedBalance.balance()
        );
    }

    @Test
    void testGetBalanceUserNotFound() {
        // Act & Assert
        assertThrows(
            NotFoundException.class,
            () -> userService.getBalance(999L)
        );
    }
}
