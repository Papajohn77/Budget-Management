package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
import gr.aueb.budgetmanagement.application.repositories.RecurringExpenseRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.AddedRecurringExpenseRepresentation;
import gr.aueb.budgetmanagement.domain.entities.RecurringExpense;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;

@QuarkusTest
class RecurringExpenseServiceTest {
    private static final ExpenseCategory VALID_CATEGORY = ExpenseCategory.HOUSING;
    private static final Money VALID_MONEY = new Money(new BigDecimal("1500.00"));

    @Inject
    EntityManager entityManager;

    @Inject
    private UserRepository userRepository;

    @Inject
    private RecurringExpenseRepository recurringExpenseRepository;

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

    @Test
    @TestTransaction
    void testApplyRecurringExpensesSuccessful() {
        // Arrange
        LocalDate today = LocalDate.now();
        RecurringExpense recurringExpense = RecurringExpense.create(
            "New Test Expense",
            VALID_MONEY,
            VALID_CATEGORY,
            today,  // Start date is today
            today.plusMonths(12),
            user
        );
        recurringExpenseRepository.save(recurringExpense);

        // Act
        recurringExpenseService.applyRecurringExpenses(today);

        // Assert
        RecurringExpense updated = entityManager.find(RecurringExpense.class, recurringExpense.getId());
        assertNotNull(updated.getLastAppliedDate());
        assertEquals(today, updated.getLastAppliedDate());
        assertEquals(1, updated.getGeneratedExpenses().size());
    }

    @Test
    @TestTransaction
    void testApplyRecurringExpensesWithoutEligibleExpenses() {
        // Arrange
        LocalDate today = LocalDate.now();
        RecurringExpense futureExpense = RecurringExpense.create(
            "Future Expense",
            VALID_MONEY,
            VALID_CATEGORY,
            today.plusDays(10),
            today.plusMonths(12),
            user
        );
        recurringExpenseRepository.save(futureExpense);

        long expenseCountBefore = countExpenses();
        
        // Act
        recurringExpenseService.applyRecurringExpenses(today);

        // Assert
        long expenseCountAfter = countExpenses();
        assertEquals(expenseCountBefore, expenseCountAfter);
        
        RecurringExpense updated = entityManager.find(RecurringExpense.class, futureExpense.getId());
        assertNull(updated.getLastAppliedDate());
        assertTrue(updated.getGeneratedExpenses().isEmpty());
    }

    @Test
    @TestTransaction
    void testApplyRecurringExpensesWithMultipleExpenses() {
        // Arrange
        LocalDate today = LocalDate.now();

        RecurringExpense applicableExpense = RecurringExpense.create(
            "Applicable Expense",
            VALID_MONEY,
            VALID_CATEGORY,
            today,
            today.plusMonths(12),
            user
        );
        recurringExpenseRepository.save(applicableExpense);

        RecurringExpense nonApplicableExpense = RecurringExpense.create(
            "Non-applicable Expense",
            VALID_MONEY,
            VALID_CATEGORY,
            today.plusDays(10),
            today.plusMonths(12),
            user
        );
        recurringExpenseRepository.save(nonApplicableExpense);
        
        long expenseCountBefore = countExpenses();
        
        // Act
        recurringExpenseService.applyRecurringExpenses(today);
        
        // Assert
        long expenseCountAfter = countExpenses();
        assertEquals(expenseCountBefore + 1, expenseCountAfter);
        
        RecurringExpense updatedApplicable = entityManager.find(RecurringExpense.class, applicableExpense.getId());
        assertNotNull(updatedApplicable.getLastAppliedDate());
        assertEquals(1, updatedApplicable.getGeneratedExpenses().size());

        RecurringExpense updatedNonApplicable = entityManager.find(RecurringExpense.class, nonApplicableExpense.getId());
        assertNull(updatedNonApplicable.getLastAppliedDate());
        assertTrue(updatedNonApplicable.getGeneratedExpenses().isEmpty());
    }

    @Test
    @TestTransaction
    void testApplyRecurringExpensesTwoConsecutiveApplications() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate lastMonth = today.minusMonths(1);

        RecurringExpense recurringExpense = RecurringExpense.create(
            "Monthly Expense",
            VALID_MONEY,
            VALID_CATEGORY,
            lastMonth,
            lastMonth.plusMonths(12),
            user
        );

        // Manually set up the first month's application
        recurringExpense.apply(lastMonth); // This will update lastAppliedDate and create an expense

        recurringExpenseRepository.save(recurringExpense);

        // Verify initial state
        assertEquals(lastMonth, recurringExpense.getLastAppliedDate());
        assertEquals(1, recurringExpense.getGeneratedExpenses().size());

        long expenseCountBefore = countExpenses();

        // Act - apply for current month
        recurringExpenseService.applyRecurringExpenses(today);

        // Assert
        long expenseCountAfter = countExpenses();
        assertEquals(expenseCountBefore + 1, expenseCountAfter);

        RecurringExpense updated = entityManager.find(RecurringExpense.class, recurringExpense.getId());
        assertEquals(today, updated.getLastAppliedDate());
        assertEquals(2, updated.getGeneratedExpenses().size());
    }

    @Test
    @TestTransaction
    void testApplyRecurringExpensesExtraApplicationForPartialMonth() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(2);
        // This should allow 3 applications (2 full months + partial month)
        LocalDate endDate = startDate.plusMonths(2).plusDays(5);
        
        RecurringExpense recurringExpense = RecurringExpense.create(
            "Partial Month Expense",
            VALID_MONEY,
            VALID_CATEGORY,
            startDate,
            endDate,
            user
        );
        
        // Apply first time for initial month
        recurringExpense.apply(startDate);
        recurringExpenseRepository.save(recurringExpense);
        
        // Verify first application
        assertEquals(startDate, recurringExpense.getLastAppliedDate());
        assertEquals(1, recurringExpense.getGeneratedExpenses().size());
        
        // Act - apply for second month
        recurringExpenseService.applyRecurringExpenses(startDate.plusMonths(1));
        
        // Verify second application
        RecurringExpense afterSecond = entityManager.find(RecurringExpense.class, recurringExpense.getId());
        assertEquals(startDate.plusMonths(1), afterSecond.getLastAppliedDate());
        assertEquals(2, afterSecond.getGeneratedExpenses().size());
        
        // Apply for third month
        recurringExpenseService.applyRecurringExpenses(startDate.plusMonths(2));
        
        // Assert - verify third application was allowed
        RecurringExpense afterThird = entityManager.find(RecurringExpense.class, recurringExpense.getId());
        assertEquals(startDate.plusMonths(2), afterThird.getLastAppliedDate());
        assertEquals(3, afterThird.getGeneratedExpenses().size());
        
        // Try to apply for fourth month (should not be allowed)
        recurringExpenseService.applyRecurringExpenses(startDate.plusMonths(3));
        
        // Assert - verify fourth application was not allowed
        RecurringExpense afterFourth = entityManager.find(RecurringExpense.class, recurringExpense.getId());
        assertEquals(startDate.plusMonths(2), afterFourth.getLastAppliedDate());
        assertEquals(3, afterFourth.getGeneratedExpenses().size());
    }

    private long countExpenses() {
        return entityManager
            .createQuery("SELECT COUNT(e) FROM Expense e", Long.class)
            .getSingleResult();
    }
}
