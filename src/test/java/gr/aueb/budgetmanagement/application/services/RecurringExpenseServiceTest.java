package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.commands.AddRecurringExpenseCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.AddedRecurringExpenseRepresentation;
import gr.aueb.budgetmanagement.domain.entities.RecurringExpense;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

@QuarkusTest
class RecurringExpenseServiceTest {
    private static final ExpenseCategory VALID_CATEGORY = ExpenseCategory.FOOD;
    private static final Money VALID_MONEY = new Money(new BigDecimal("9.99"));

    @Inject
    private UserRepository userRepository;

    @Inject
    private RecurringExpenseService recurringExpenseService;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
    }

    @Test
    @TestTransaction
    void createRecurringExpenseWithValidData() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        AddRecurringExpenseCommand command = new AddRecurringExpenseCommand(
            "Netflix Subscription",
            VALID_MONEY,
            VALID_CATEGORY,
            startDate,
            endDate,
            user.getId()
        );

        // Act
        AddedRecurringExpenseRepresentation result = recurringExpenseService.createRecurringExpense(command);

        // Assert
        assertNotNull(result.id());
        assertEquals("Netflix Subscription", result.name());
        assertEquals(VALID_MONEY, result.amount());
        assertEquals(VALID_CATEGORY, result.category());
        assertEquals(startDate, result.startDate());
        assertEquals(endDate, result.endDate());
        assertFalse(result.isStopped());

        // Verify the recurring expense was saved in the database
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
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
    @TestTransaction
    void createRecurringExpense_WithNonExistentUser() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        AddRecurringExpenseCommand command = new AddRecurringExpenseCommand(
            "Gym Membership",
            VALID_MONEY,
            VALID_CATEGORY,
            startDate,
            endDate,
            999L
        );

        // Act & Assert
        assertThrows(
            NotFoundException.class,
            () -> recurringExpenseService.createRecurringExpense(command)
        );
    }

    @Test
    @TestTransaction
    void createRecurringExpense_WithInvalidCommand() {
        // Arrange - Create command with empty name
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        AddRecurringExpenseCommand command = new AddRecurringExpenseCommand(
            "", // Empty name should be invalid
            VALID_MONEY,
            VALID_CATEGORY,
            startDate,
            endDate, 
            user.getId()
        );

        // Act & Assert - Assuming RecurringExpense.create() validates the name
        assertThrows(
            ConstraintViolationException.class,
            () -> recurringExpenseService.createRecurringExpense(command)
        );
    }

    @Test
    @TestTransaction
    void createRecurringExpense_WithInvalidDateRange() {
        // Arrange - Create command with end date before start date
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.minusMonths(1); // End date before start date
        AddRecurringExpenseCommand command = new AddRecurringExpenseCommand(
            "Netflix Subscription",
            VALID_MONEY,
            VALID_CATEGORY,
            startDate,
            endDate, 
            user.getId()
        );

        assertThrows(
            Exception.class,
            () -> recurringExpenseService.createRecurringExpense(command)
        );
    }
}
