package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.repositories.RecurringExpenseRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.RecurringExpense;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
class JpaRecurringExpenseRepositoryTest {
    private static final Money VALID_AMOUNT = new Money(new BigDecimal("150.00"));
    private static final ExpenseCategory VALID_CATEGORY = ExpenseCategory.HOUSING;

    @Inject
    private EntityManager entityManager;

    @Inject
    private UserRepository userRepository;

    @Inject
    private RecurringExpenseRepository recurringExpenseRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
    }

    @Test
    @TestTransaction
    void testSave() {
        // Arrange
        String uniqueName = "Test Recurring Expense " + System.currentTimeMillis();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);

        RecurringExpense recurringExpense = RecurringExpense.create(
            uniqueName,
            VALID_AMOUNT,
            VALID_CATEGORY,
            startDate,
            endDate,
            user
        );

        // Act
        recurringExpenseRepository.save(recurringExpense);

        // Assert
        assertNotNull(recurringExpense.getId());

        // Verify it's in the database
        RecurringExpense foundExpense = entityManager.find(RecurringExpense.class, recurringExpense.getId());
        assertNotNull(foundExpense);
        assertEquals(uniqueName, foundExpense.getName());
        assertEquals(VALID_AMOUNT.getValue(), foundExpense.getAmount().getValue());
        assertEquals(VALID_CATEGORY, foundExpense.getCategory());
        assertEquals(startDate, foundExpense.getStartDate());
        assertEquals(endDate, foundExpense.getEndDate());
        assertEquals(user.getId(), foundExpense.getUser().getId());
    }

    @Test
    @TestTransaction
    void testFindNonStoppedRecurringExpenses_Empty() {
        // Arrange
        String uniqueName = "Test Recurring Expense " + System.currentTimeMillis();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);

        RecurringExpense recurringExpense = RecurringExpense.create(
            uniqueName,
            VALID_AMOUNT,
            VALID_CATEGORY,
            startDate,
            endDate,
            user
        );
        recurringExpense.stop(true);

        entityManager.persist(recurringExpense);

        // Act
        List<RecurringExpense> result = recurringExpenseRepository.findNonStoppedRecurringExpenses();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @TestTransaction
    void testFindNonStoppedRecurringExpenses_WithActive() {
        // Arrange
        String uniqueName1 = "Active Expense " + System.currentTimeMillis();
        String uniqueName2 = "Stopped Expense " + System.currentTimeMillis();
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);

        // Create active recurring expense
        RecurringExpense activeExpense = RecurringExpense.create(
            uniqueName1,
            VALID_AMOUNT,
            VALID_CATEGORY,
            startDate,
            endDate,
            user
        );

        // Create stopped recurring expense
        RecurringExpense stoppedExpense = RecurringExpense.create(
            uniqueName2,
            VALID_AMOUNT,
            VALID_CATEGORY,
            startDate,
            endDate,
            user
        );
        stoppedExpense.stop(true);

        // Save both
        entityManager.persist(activeExpense);
        entityManager.persist(stoppedExpense);

        // Act
        List<RecurringExpense> result = recurringExpenseRepository.findNonStoppedRecurringExpenses();

        // Assert
        assertFalse(result.isEmpty());

        // Check if our active expense is in the result
        boolean containsActiveExpense = result.stream()
            .anyMatch(re -> re.getId().equals(activeExpense.getId()));
        assertTrue(containsActiveExpense);

        // Check that our stopped expense is NOT in the result
        boolean containsStoppedExpense = result.stream()
            .anyMatch(re -> re.getId().equals(stoppedExpense.getId()));
        assertFalse(containsStoppedExpense);
    }

    @Test
    @TestTransaction
    void testFindNonStoppedRecurringExpenses_MultipleActive() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);

        // Create multiple active recurring expenses
        List<RecurringExpense> createdExpenses = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            RecurringExpense expense = RecurringExpense.create(
                "Active Expense " + i,
                VALID_AMOUNT,
                VALID_CATEGORY,
                startDate,
                endDate,
                user
            );
            entityManager.persist(expense);
            createdExpenses.add(expense);
        }

        // Act
        List<RecurringExpense> result = recurringExpenseRepository.findNonStoppedRecurringExpenses();

        // Assert
        assertFalse(result.isEmpty());

        // Check if all our created expenses are in the result
        Set<Long> resultIds = result.stream()
            .map(RecurringExpense::getId)
            .collect(Collectors.toSet());

        for (RecurringExpense expense : createdExpenses) {
            assertTrue(resultIds.contains(expense.getId()), 
                "Result should contain active expense with ID " + expense.getId());
        }
    }
}
