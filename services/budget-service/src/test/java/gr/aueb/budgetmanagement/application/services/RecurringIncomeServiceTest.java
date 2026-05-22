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
import gr.aueb.budgetmanagement.application.commands.AddRecurringIncomeCommand;
import gr.aueb.budgetmanagement.application.commands.UpdateRecurringIncomeCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.RecurringIncomeRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.RecurringIncomeRepresentation;
import gr.aueb.budgetmanagement.domain.entities.RecurringIncome;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;

@QuarkusTest
class RecurringIncomeServiceTest {
    private static final IncomeCategory VALID_CATEGORY = IncomeCategory.SALARY;
    private static final Money VALID_MONEY = new Money(new BigDecimal("2500.00"));

    @Inject
    EntityManager entityManager;

    @Inject
    private UserRepository userRepository;

    @Inject
    private RecurringIncomeRepository recurringIncomeRepository;

    @Inject
    private RecurringIncomeService recurringIncomeService;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
    }

    @Test
    @TestTransaction
    void createRecurringIncomeWithValidData() {
        // Arrange
        int originalIncomeCount = user.getRecurringIncomes().size();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        String incomeName = "Monthly Salary " + System.currentTimeMillis(); // Ensure unique name

        AddRecurringIncomeCommand command = new AddRecurringIncomeCommand(
                incomeName,
                VALID_MONEY,
                VALID_CATEGORY,
                startDate,
                endDate,
                user.getId());

        // Act
        RecurringIncomeRepresentation result = recurringIncomeService.createRecurringIncome(command);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertNotNull(result.id(), "The recurring income ID should not be null");

        User refreshedUser = userRepository.findById(user.getId()).orElseThrow();

        assertEquals(originalIncomeCount + 1, refreshedUser.getRecurringIncomes().size(),
                "User should have one more recurring income");

        RecurringIncome savedIncome = refreshedUser.getRecurringIncomes().stream()
                .filter(e -> incomeName.equals(e.getName()))
                .findFirst()
                .orElse(null);

        assertNotNull(savedIncome, "Saved income should exist in the database");
        assertNotNull(savedIncome.getId(), "Saved income ID should not be null");

        assertEquals(incomeName, result.name());
        assertEquals(VALID_MONEY.getValue(), result.amount());
        assertEquals(VALID_CATEGORY, result.category());
        assertEquals(startDate, result.startDate());
        assertEquals(endDate, result.endDate());
        assertFalse(result.isStopped());
    }

    @Test
    @TestTransaction
    void createRecurringIncome_WithNonExistentUser() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        AddRecurringIncomeCommand command = new AddRecurringIncomeCommand(
                "Freelance Income",
                VALID_MONEY,
                VALID_CATEGORY,
                startDate,
                endDate,
                999L);

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> recurringIncomeService.createRecurringIncome(command)
        );
    }

    @Test
    @TestTransaction
    void createRecurringIncome_WithInvalidCommand() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        AddRecurringIncomeCommand command = new AddRecurringIncomeCommand(
                "",
                VALID_MONEY,
                VALID_CATEGORY,
                startDate,
                endDate,
                user.getId()
        );

        // Act & Assert - Assuming RecurringIncome.create() validates the name
        assertThrows(
                ConstraintViolationException.class,
                () -> recurringIncomeService.createRecurringIncome(command)
        );
    }

    @Test
    @TestTransaction
    void createRecurringIncome_WithInvalidDateRange() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.minusMonths(1); // End date before start date
        AddRecurringIncomeCommand command = new AddRecurringIncomeCommand(
                "Monthly Salary",
                VALID_MONEY,
                VALID_CATEGORY,
                startDate,
                endDate,
                user.getId()
        );

        assertThrows(
                Exception.class,
                () -> recurringIncomeService.createRecurringIncome(command)
        );
    }

    @Test
    @TestTransaction
    void getRecurringIncomes_WithNonExistentUser_ThrowsNotFoundException() {
        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> recurringIncomeService.getRecurringIncomes(999L)
        );
    }

    @Test
    @TestTransaction
    void updateRecurringIncome_WithNonExistentUser_ThrowsNotFoundException() {
        UpdateRecurringIncomeCommand command = new UpdateRecurringIncomeCommand(
                1L, // Any ID
                999L, // Non-existent user
                true
        );

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> recurringIncomeService.updateRecurringIncome(command)
        );
    }

    @Test
    @TestTransaction
    void updateRecurringIncome_WithNonExistentIncome_ThrowsNotFoundException() {
        UpdateRecurringIncomeCommand command = new UpdateRecurringIncomeCommand(
                999L, // Non-existent income
                user.getId(),
                true
        );

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> recurringIncomeService.updateRecurringIncome(command)
        );
    }

    @Test
    @TestTransaction
    void deleteRecurringIncome_WithNonExistentUser_ThrowsNotFoundException() {
        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> recurringIncomeService.deleteRecurringIncome(1L, 999L)
        );
    }

    @Test
    @TestTransaction
    void deleteRecurringIncome_WithNonExistentIncome_ThrowsNotFoundException() {
        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> recurringIncomeService.deleteRecurringIncome(999L, user.getId())
        );
    }

    @Test
    @TestTransaction
    void deleteNonExistentRecurringIncome_WithValidUser() {
        Long nonExistentIncomeId = 9999L;

        assertThrows(
                NotFoundException.class,
                () -> recurringIncomeService.deleteRecurringIncome(nonExistentIncomeId, user.getId())
        );
    }

    @Test
    @TestTransaction
    void getRecurringIncomes_WithValidUser_ReturnsRepresentation() {
        // Act
        var representation = recurringIncomeService.getRecurringIncomes(user.getId());

        // Assert
        assertNotNull(representation, "Representation should not be null");
        assertNotNull(representation.recurringIncomes(), "Income list should not be null");
        assertEquals(user.getRecurringIncomes().size(), representation.recurringIncomes().size(),
                "Representation should contain all user's recurring incomes");
    }

    @Test
    @TestTransaction
    void updateRecurringIncome_WithValidData_StopsRecurringIncome() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        String incomeName = "Income to Stop " + System.currentTimeMillis();

        // First create a recurring income
        AddRecurringIncomeCommand createCommand = new AddRecurringIncomeCommand(
                incomeName,
                VALID_MONEY,
                VALID_CATEGORY,
                startDate,
                endDate,
                user.getId());

        RecurringIncomeRepresentation created = recurringIncomeService.createRecurringIncome(createCommand);
        assertFalse(created.isStopped(), "New income should not be stopped initially");

        // Act - Update to stop the income
        UpdateRecurringIncomeCommand updateCommand = new UpdateRecurringIncomeCommand(
                created.id(),
                user.getId(),
                true
        );

        recurringIncomeService.updateRecurringIncome(updateCommand);

        // Assert
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        RecurringIncome updatedIncome = updatedUser.getRecurringIncomes().stream()
                .filter(ri -> ri.getId().equals(created.id()))
                .findFirst()
                .orElseThrow();

        assertTrue(updatedIncome.isStopped(), "Income should be stopped after update");
    }

    @Test
    @TestTransaction
    void deleteRecurringIncome_WithValidData_RemovesRecurringIncome() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        String incomeName = "Income to Delete " + System.currentTimeMillis();

        // First create a recurring income
        AddRecurringIncomeCommand createCommand = new AddRecurringIncomeCommand(
                incomeName,
                VALID_MONEY,
                VALID_CATEGORY,
                startDate,
                endDate,
                user.getId());

        RecurringIncomeRepresentation created = recurringIncomeService.createRecurringIncome(createCommand);
        int originalCount = user.getRecurringIncomes().size();

        // Act
        recurringIncomeService.deleteRecurringIncome(created.id(), user.getId());

        // Assert
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(originalCount, updatedUser.getRecurringIncomes().size(),
                "User should have one less recurring income after deletion");

        boolean incomeExists = updatedUser.getRecurringIncomes().stream()
                .anyMatch(ri -> ri.getId().equals(created.id()));

        assertFalse(incomeExists, "The recurring income should no longer exist");
    }

    @Test
    @TestTransaction
    void testApplyRecurringIncomesSuccessful() {
        // Arrange
        LocalDate today = LocalDate.now();
        RecurringIncome recurringIncome = RecurringIncome.create(
            "New Test Income",
            VALID_MONEY,
            VALID_CATEGORY,
            today,  // Start date is today
            today.plusMonths(12),
            user
        );
        recurringIncomeRepository.save(recurringIncome);

        // Act
        recurringIncomeService.applyRecurringIncomes(today);

        // Assert
        RecurringIncome updated = entityManager.find(RecurringIncome.class, recurringIncome.getId());
        assertNotNull(updated.getLastAppliedDate());
        assertEquals(today, updated.getLastAppliedDate());
        assertEquals(1, updated.getGeneratedIncomes().size());
    }

    @Test
    @TestTransaction
    void testApplyRecurringIncomesWithoutEligibleIncomes() {
        // Arrange
        LocalDate today = LocalDate.now();
        RecurringIncome futureIncome = RecurringIncome.create(
            "Future Income",
            VALID_MONEY,
            VALID_CATEGORY,
            today.plusDays(10),
            today.plusMonths(12),
            user
        );
        recurringIncomeRepository.save(futureIncome);

        long incomeCountBefore = countIncomes();

        // Act
        recurringIncomeService.applyRecurringIncomes(today);

        // Assert
        long incomeCountAfter = countIncomes();
        assertEquals(incomeCountBefore, incomeCountAfter);

        RecurringIncome updated = entityManager.find(RecurringIncome.class, futureIncome.getId());
        assertNull(updated.getLastAppliedDate());
        assertTrue(updated.getGeneratedIncomes().isEmpty());
    }

    @Test
    @TestTransaction
    void testApplyRecurringIncomesWithMultipleIncomes() {
        // Arrange
        LocalDate today = LocalDate.now();

        RecurringIncome applicableIncome = RecurringIncome.create(
            "Applicable Income",
            VALID_MONEY,
            VALID_CATEGORY,
            today,
            today.plusMonths(12),
            user
        );
        recurringIncomeRepository.save(applicableIncome);

        RecurringIncome nonApplicableIncome = RecurringIncome.create(
            "Non-applicable Income",
            VALID_MONEY,
            VALID_CATEGORY,
            today.plusDays(10),
            today.plusMonths(12),
            user
        );
        recurringIncomeRepository.save(nonApplicableIncome);

        long incomeCountBefore = countIncomes();

        // Act
        recurringIncomeService.applyRecurringIncomes(today);

        // Assert
        long incomeCountAfter = countIncomes();
        assertEquals(incomeCountBefore + 1, incomeCountAfter);

        RecurringIncome updatedApplicable = entityManager.find(RecurringIncome.class, applicableIncome.getId());
        assertNotNull(updatedApplicable.getLastAppliedDate());
        assertEquals(1, updatedApplicable.getGeneratedIncomes().size());

        RecurringIncome updatedNonApplicable = entityManager.find(RecurringIncome.class, nonApplicableIncome.getId());
        assertNull(updatedNonApplicable.getLastAppliedDate());
        assertTrue(updatedNonApplicable.getGeneratedIncomes().isEmpty());
    }

    @Test
    @TestTransaction
    void testApplyRecurringIncomesTwoConsecutiveApplications() {
        // Arrange
        LocalDate today = LocalDate.now();
        LocalDate lastMonth = today.minusMonths(1);

        RecurringIncome recurringIncome = RecurringIncome.create(
                "Monthly Income",
                VALID_MONEY,
                VALID_CATEGORY,
                lastMonth,
                lastMonth.plusMonths(12),
                user
        );

        // Manually set up the first month's application
        recurringIncome.apply(lastMonth); // This will update lastAppliedDate and create an income

        recurringIncomeRepository.save(recurringIncome);

        // Verify initial state
        assertEquals(lastMonth, recurringIncome.getLastAppliedDate());
        assertEquals(1, recurringIncome.getGeneratedIncomes().size());

        long incomeCountBefore = countIncomes();

        // Act - apply for current month
        recurringIncomeService.applyRecurringIncomes(today);

        // Assert
        long incomeCountAfter = countIncomes();
        assertEquals(incomeCountBefore + 1, incomeCountAfter);

        RecurringIncome updated = entityManager.find(RecurringIncome.class, recurringIncome.getId());
        assertEquals(today, updated.getLastAppliedDate());
        assertEquals(2, updated.getGeneratedIncomes().size());
    }

    @Test
    @TestTransaction
    void testApplyRecurringIncomesExtraApplicationForPartialMonth() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(2);
        // This should allow 3 applications (2 full months + partial month)
        LocalDate endDate = startDate.plusMonths(2).plusDays(5);
        
        RecurringIncome recurringIncome = RecurringIncome.create(
            "Partial Month Income",
            VALID_MONEY,
            VALID_CATEGORY,
            startDate,
            endDate,
            user
        );
        
        // Apply first time for initial month
        recurringIncome.apply(startDate);
        recurringIncomeRepository.save(recurringIncome);
        
        // Verify first application
        assertEquals(startDate, recurringIncome.getLastAppliedDate());
        assertEquals(1, recurringIncome.getGeneratedIncomes().size());
        
        // Act - apply for second month
        recurringIncomeService.applyRecurringIncomes(startDate.plusMonths(1));
        
        // Verify second application
        RecurringIncome afterSecond = entityManager.find(RecurringIncome.class, recurringIncome.getId());
        assertEquals(startDate.plusMonths(1), afterSecond.getLastAppliedDate());
        assertEquals(2, afterSecond.getGeneratedIncomes().size());
        
        // Apply for third month
        recurringIncomeService.applyRecurringIncomes(startDate.plusMonths(2));
        
        // Assert - verify third application was allowed
        RecurringIncome afterThird = entityManager.find(RecurringIncome.class, recurringIncome.getId());
        assertEquals(startDate.plusMonths(2), afterThird.getLastAppliedDate());
        assertEquals(3, afterThird.getGeneratedIncomes().size());
        
        // Try to apply for fourth month (should not be allowed)
        recurringIncomeService.applyRecurringIncomes(startDate.plusMonths(3));
        
        // Assert - verify fourth application was not allowed
        RecurringIncome afterFourth = entityManager.find(RecurringIncome.class, recurringIncome.getId());
        assertEquals(startDate.plusMonths(2), afterFourth.getLastAppliedDate());
        assertEquals(3, afterFourth.getGeneratedIncomes().size());
    }

    private long countIncomes() {
    return entityManager
        .createQuery("SELECT COUNT(i) FROM Income i", Long.class)
        .getSingleResult();
    }
}
