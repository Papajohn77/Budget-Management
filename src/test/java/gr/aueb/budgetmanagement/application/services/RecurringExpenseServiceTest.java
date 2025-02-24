package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.LocalDate;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.ports.PasswordHasher;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.application.commands.CreateRecurringExpenseCommand;
import gr.aueb.budgetmanagement.application.dto.CreatedRecurringExpenseDTO;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.domain.entities.RecurringExpense;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaUserRepository;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class RecurringExpenseServiceTest {
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test123!@#";
    private static final ExpenseCategory VALID_CATEGORY = ExpenseCategory.FOOD;
    private static final Money VALID_MONEY = new Money(new BigDecimal("9.99"));

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private JpaUserRepository userRepository;
    private RecurringExpenseService recurringExpenseService;
    private PasswordHasher passwordHasher;
    private User user;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        userRepository = new JpaUserRepository(entityManager);
        recurringExpenseService = new RecurringExpenseService(userRepository);
        passwordHasher = new BCryptPasswordEncoder();

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
        user = User.create(
                TEST_USERNAME,
                TEST_EMAIL,
                TEST_PASSWORD,
                new BCryptPasswordEncoder());

        entityManager.persist(user);
    }

    @Test
    void createRecurringExpense_WithValidData() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        CreateRecurringExpenseCommand command = new CreateRecurringExpenseCommand(
                user.getId(),
                "Netflix Subscription",
                VALID_MONEY,
                VALID_CATEGORY,
                startDate,
                endDate
        );

        // Act
        CreatedRecurringExpenseDTO result = recurringExpenseService.createRecurringExpense(command);

        // Assert
        assertNotNull(result.id());
        assertEquals("Netflix Subscription", result.name());
        assertEquals(VALID_MONEY, result.amount());
        assertEquals(VALID_CATEGORY, result.category());
        assertEquals(startDate, result.startDate());
        assertEquals(endDate, result.endDate());
        //assertFalse(result.stopped());

        // Verify the recurring expense was saved in the database
        User updatedUser = entityManager.find(User.class, user.getId());
        assertNotNull(updatedUser);

        RecurringExpense savedExpense = null;
        for (RecurringExpense expense : updatedUser.getRecurringExpenses()) {
            if (expense.getId().equals(result.id())) {
                savedExpense = expense;
                break;
            }
        }

        assertNotNull(savedExpense);
        assertEquals("Netflix Subscription", savedExpense.getName());
        assertEquals(VALID_MONEY, savedExpense.getAmount());
        assertEquals(VALID_CATEGORY, savedExpense.getCategory());
        assertEquals(startDate, savedExpense.getStartDate());
        assertEquals(endDate, savedExpense.getEndDate());
        assertFalse(savedExpense.isStopped());
    }

    @Test
    void createRecurringExpense_WithNonExistentUser() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        CreateRecurringExpenseCommand command = new CreateRecurringExpenseCommand(
                999L,
                "Gym Membership",
                VALID_MONEY,
                VALID_CATEGORY,
                startDate,
                endDate
        );

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> recurringExpenseService.createRecurringExpense(command)
        );
    }

    @Test
    void createRecurringExpense_WithInvalidCommand() {
        // Arrange - Create command with empty name
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        CreateRecurringExpenseCommand command = new CreateRecurringExpenseCommand(
                user.getId(),
                "", // Empty name should be invalid
                VALID_MONEY,
                VALID_CATEGORY,
                startDate,
                endDate
        );

        // Act & Assert - Assuming RecurringExpense.create() validates the name
        assertThrows(
                InvalidDomainArgumentException.class,
                () -> recurringExpenseService.createRecurringExpense(command)
        );
    }

    @Test
    void createRecurringExpense_WithInvalidDateRange() {
        // Arrange - Create command with end date before start date
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.minusMonths(1); // End date before start date
        CreateRecurringExpenseCommand command = new CreateRecurringExpenseCommand(
                user.getId(),
                "Netflix Subscription",
                VALID_MONEY,
                VALID_CATEGORY,
                startDate,
                endDate
        );

        // Act & Assert - Assuming RecurringExpense.create() validates the date range
        assertThrows(
                Exception.class, // Replace with the specific exception your domain throws
                () -> recurringExpenseService.createRecurringExpense(command)
        );
    }
}