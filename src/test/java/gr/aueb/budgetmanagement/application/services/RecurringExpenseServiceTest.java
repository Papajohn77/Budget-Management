package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.commands.AddRecurringExpenseCommand;
import gr.aueb.budgetmanagement.application.commands.UpdateRecurringExpenseCommand;
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
    private static final ExpenseCategory VALID_CATEGORY = ExpenseCategory.HOUSING;
    private static final Money VALID_MONEY = new Money(new BigDecimal("1500.00"));

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
        int originalExpenseCount = user.getRecurringExpenses().size();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        String expenseName = "Monthly Rent " + System.currentTimeMillis(); // Ensure unique name

        AddRecurringExpenseCommand command = new AddRecurringExpenseCommand(
                expenseName,
                VALID_MONEY,
                VALID_CATEGORY,
                startDate,
                endDate,
                user.getId());

        // Act
        AddedRecurringExpenseRepresentation result = recurringExpenseService.createRecurringExpense(command);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.id(), "The recurring expense ID should not be null");

        User refreshedUser = userRepository.findById(user.getId()).orElseThrow();

        assertEquals(originalExpenseCount + 1, refreshedUser.getRecurringExpenses().size(),
                "User should have one more recurring expense");

        RecurringExpense savedExpense = refreshedUser.getRecurringExpenses().stream()
                .filter(e -> expenseName.equals(e.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull(savedExpense, "Saved expense should exist in the database");
        assertNotNull(savedExpense.getId(), "Saved expense ID should not be null");

        assertEquals(expenseName, result.name());
        assertEquals(VALID_MONEY.getValue(), result.amount());
        assertEquals(VALID_CATEGORY, result.category());
        assertEquals(startDate, result.startDate());
        assertEquals(endDate, result.endDate());
        assertFalse(result.isStopped());
    }

    @Test
    @TestTransaction
    void createRecurringExpense_WithNonExistentUser() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        AddRecurringExpenseCommand command = new AddRecurringExpenseCommand(
                "Entertainment Subscription",
                VALID_MONEY,
                ExpenseCategory.ENTERTAINMENT,
                startDate,
                endDate,
                999L);

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> recurringExpenseService.createRecurringExpense(command)
        );
    }

    @Test
    @TestTransaction
    void createRecurringExpense_WithInvalidCommand() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        AddRecurringExpenseCommand command = new AddRecurringExpenseCommand(
                "",
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
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.minusMonths(1); // End date before start date
        AddRecurringExpenseCommand command = new AddRecurringExpenseCommand(
                "Monthly Rent",
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

    @Test
    @TestTransaction
    void getRecurringExpenses_WithNonExistentUser_ThrowsNotFoundException() {
        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> recurringExpenseService.getRecurringExpenses(999L)
        );
    }

    @Test
    @TestTransaction
    void updateRecurringExpense_WithNonExistentUser_ThrowsNotFoundException() {
        UpdateRecurringExpenseCommand command = new UpdateRecurringExpenseCommand(
                1L, // Any ID
                999L, // Non-existent user
                true
        );

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> recurringExpenseService.updateRecurringExpense(command)
        );
    }

    @Test
    @TestTransaction
    void updateRecurringExpense_WithNonExistentExpense_ThrowsNotFoundException() {
        UpdateRecurringExpenseCommand command = new UpdateRecurringExpenseCommand(
                999L, // Non-existent expense
                user.getId(),
                true
        );

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> recurringExpenseService.updateRecurringExpense(command)
        );
    }

    @Test
    @TestTransaction
    void deleteRecurringExpense_WithNonExistentUser_ThrowsNotFoundException() {
        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> recurringExpenseService.deleteRecurringExpense(1L, 999L)
        );
    }

    @Test
    @TestTransaction
    void deleteRecurringExpense_WithNonExistentExpense_ThrowsNotFoundException() {
        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> recurringExpenseService.deleteRecurringExpense(999L, user.getId())
        );
    }

    @Test
    @TestTransaction
    void deleteNonExistentRecurringExpense_WithValidUser() {
        Long nonExistentExpenseId = 9999L;

        assertThrows(
                NotFoundException.class,
                () -> recurringExpenseService.deleteRecurringExpense(nonExistentExpenseId, user.getId())
        );
    }

    @Test
    @TestTransaction
    void getRecurringExpenses_WithValidUser_ReturnsRepresentation() {
        // Act
        var representation = recurringExpenseService.getRecurringExpenses(user.getId());

        // Assert
        assertNotNull(representation, "Representation should not be null");
        assertNotNull(representation.recurringExpenses(), "Expense list should not be null");
        assertEquals(user.getRecurringExpenses().size(), representation.recurringExpenses().size(),
                "Representation should contain all user's recurring expenses");
    }

    @Test
    @TestTransaction
    void updateRecurringExpense_WithValidData_StopsRecurringExpense() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        String expenseName = "Expense to Stop " + System.currentTimeMillis();

        // First create a recurring expense
        AddRecurringExpenseCommand createCommand = new AddRecurringExpenseCommand(
                expenseName,
                VALID_MONEY,
                ExpenseCategory.OTHER,
                startDate,
                endDate,
                user.getId());

        AddedRecurringExpenseRepresentation created = recurringExpenseService.createRecurringExpense(createCommand);
        assertFalse(created.isStopped(), "New expense should not be stopped initially");

        // Act - Update to stop the expense
        UpdateRecurringExpenseCommand updateCommand = new UpdateRecurringExpenseCommand(
                created.id(),
                user.getId(),
                true
        );

        recurringExpenseService.updateRecurringExpense(updateCommand);

        // Assert
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        RecurringExpense updatedExpense = updatedUser.getRecurringExpenses().stream()
                .filter(re -> re.getId().equals(created.id()))
                .findFirst()
                .orElseThrow();

        assertTrue(updatedExpense.isStopped(), "Expense should be stopped after update");
    }

    @Test
    @TestTransaction
    void deleteRecurringExpense_WithValidData_RemovesRecurringExpense() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        String expenseName = "Expense to Delete " + System.currentTimeMillis();

        // First create a recurring expense
        AddRecurringExpenseCommand createCommand = new AddRecurringExpenseCommand(
                expenseName,
                VALID_MONEY,
                ExpenseCategory.ENTERTAINMENT,
                startDate,
                endDate,
                user.getId());

        AddedRecurringExpenseRepresentation created = recurringExpenseService.createRecurringExpense(createCommand);
        int originalCount = user.getRecurringExpenses().size();

        // Act
        recurringExpenseService.deleteRecurringExpense(created.id(), user.getId());

        // Assert
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(originalCount, updatedUser.getRecurringExpenses().size(),
                "User should have one less recurring expense after deletion");

        boolean expenseExists = updatedUser.getRecurringExpenses().stream()
                .anyMatch(re -> re.getId().equals(created.id()));

        assertFalse(expenseExists, "The recurring expense should no longer exist");
    }
}