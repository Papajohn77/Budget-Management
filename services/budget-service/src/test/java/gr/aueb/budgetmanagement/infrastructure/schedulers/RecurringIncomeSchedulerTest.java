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
import gr.aueb.budgetmanagement.application.repositories.RecurringIncomeRepository;
import gr.aueb.budgetmanagement.application.services.RecurringIncomeService;
import gr.aueb.budgetmanagement.domain.entities.RecurringIncome;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
class RecurringIncomeSchedulerTest {
    @Inject
    EntityManager entityManager;

    @Inject
    private RecurringIncomeRepository recurringIncomeRepository;

    @Inject
    private RecurringIncomeService recurringIncomeService;

    @Inject
    private RecurringIncomeScheduler scheduler;

    private User user;

    @BeforeEach
    void setUp() {
        user = entityManager.find(User.class, Fixture.Users.TESTUSER_ID);
    }

    @Test
    @TestTransaction
    void testExecuteRecurringIncomeJobSuccessful() {
        // Arrange - create a recurring income eligible for application
        LocalDate startDate = LocalDate.now().minusDays(10);
        LocalDate endDate = startDate.plusMonths(12);

        RecurringIncome recurringIncome = RecurringIncome.create(
            "Test Job Income",
            new Money(new BigDecimal("200.00")),
            IncomeCategory.PASSIVE_INCOME,
            startDate,
            endDate,
            user
        );
        recurringIncomeRepository.save(recurringIncome);

        long incomeCountBefore = countIncomes();

        // Act
        scheduler.executeRecurringIncomeJob();

        // Assert
        long incomeCountAfter = countIncomes();
        assertEquals(incomeCountBefore + 1, incomeCountAfter);
    }

    @Test
    @TestTransaction
    void testRecurringIncomeSchedulerHandlesServiceException() {
        // Arrange
        // Create a spy of the real service that will throw an exception
        RecurringIncomeService serviceSpy = spy(recurringIncomeService);
        doThrow(new RuntimeException("Test exception"))
            .when(serviceSpy)
            .applyRecurringIncomes(any(LocalDate.class));
        
        // Create scheduler with the spy
        RecurringIncomeScheduler scheduler = new RecurringIncomeScheduler(serviceSpy);
        
        // Act & Assert - exception should be caught and not propagated
        assertDoesNotThrow(() -> scheduler.executeRecurringIncomeJob());
    }

    private long countIncomes() {
        return entityManager
            .createQuery("SELECT COUNT(e) FROM Income e", Long.class)
            .getSingleResult();
    }
}
