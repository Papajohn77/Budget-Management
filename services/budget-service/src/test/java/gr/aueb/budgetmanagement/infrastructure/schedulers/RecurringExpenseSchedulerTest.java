package gr.aueb.budgetmanagement.infrastructure.schedulers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.repositories.RecurringExpenseRepository;
import gr.aueb.budgetmanagement.application.services.RecurringExpenseService;
import gr.aueb.budgetmanagement.domain.entities.RecurringExpense;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
class RecurringExpenseSchedulerTest {
    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 15);
    @Inject
    EntityManager entityManager;

    @Inject
    private RecurringExpenseRepository recurringExpenseRepository;

    @Inject
    private RecurringExpenseService recurringExpenseService;

    @Inject
    private RecurringExpenseScheduler scheduler;

    private User user;

    @BeforeEach
    void setUp() {
        user = entityManager.find(User.class, Fixture.Users.TESTUSER_ID);
    }

    @Test
    @TestTransaction
    void testExecuteRecurringExpenseJobSuccessful() {
        // Arrange - create a recurring expense eligible for application
        LocalDate startDate = FIXED_DATE.minusDays(10);
        LocalDate endDate = startDate.plusMonths(12);

        RecurringExpense recurringExpense = RecurringExpense.create(
            "Test Job Expense",
            new Money(new BigDecimal("200.00")),
            ExpenseCategory.FOOD,
            startDate,
            endDate,
            user
        );
        recurringExpenseRepository.save(recurringExpense);

        long expenseCountBefore = countExpenses();

        // Act
        scheduler.executeRecurringExpenseJob();

        // Assert
        long expenseCountAfter = countExpenses();
        assertEquals(expenseCountBefore + 1, expenseCountAfter);
    }

    @Test
    @TestTransaction
    void testRecurringExpenseSchedulerHandlesServiceException() {
        // Arrange
        // Create a spy of the real service that will throw an exception
        RecurringExpenseService serviceSpy = spy(recurringExpenseService);
        doThrow(new RuntimeException("Test exception"))
            .when(serviceSpy)
            .applyRecurringExpenses(any(LocalDate.class));
        
        // Create scheduler with the spy
        RecurringExpenseScheduler scheduler = new RecurringExpenseScheduler(serviceSpy);
        
        // Act & Assert - exception should be caught and not propagated
        assertDoesNotThrow(() -> scheduler.executeRecurringExpenseJob());
    }

    private long countExpenses() {
        return entityManager
            .createQuery("SELECT COUNT(e) FROM Expense e", Long.class)
            .getSingleResult();
    }
}
