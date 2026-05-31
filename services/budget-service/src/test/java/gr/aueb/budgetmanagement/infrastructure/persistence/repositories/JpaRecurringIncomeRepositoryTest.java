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
import gr.aueb.budgetmanagement.application.repositories.RecurringIncomeRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.RecurringIncome;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
class JpaRecurringIncomeRepositoryTest {
    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 15);
    private static final Money VALID_AMOUNT = new Money(new BigDecimal("2500.00"));
    private static final IncomeCategory VALID_CATEGORY = IncomeCategory.SALARY;

    @Inject
    private EntityManager entityManager;

    @Inject
    private UserRepository userRepository;

    @Inject
    private RecurringIncomeRepository recurringIncomeRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
    }

    @Test
    @TestTransaction
    void testSave() {
        // Arrange
        String uniqueName = "Test Recurring Income " + System.currentTimeMillis();
        LocalDate startDate = FIXED_DATE;
        LocalDate endDate = startDate.plusMonths(12);

        RecurringIncome recurringIncome = RecurringIncome.create(
            uniqueName,
            VALID_AMOUNT,
            VALID_CATEGORY,
            startDate,
            endDate,
            user
        );

        // Act
        recurringIncomeRepository.save(recurringIncome);

        // Assert
        assertNotNull(recurringIncome.getId());

        // Verify it's in the database
        RecurringIncome foundIncome = entityManager.find(RecurringIncome.class, recurringIncome.getId());
        assertNotNull(foundIncome);
        assertEquals(uniqueName, foundIncome.getName());
        assertEquals(VALID_AMOUNT.getValue(), foundIncome.getAmount().getValue());
        assertEquals(VALID_CATEGORY, foundIncome.getCategory());
        assertEquals(startDate, foundIncome.getStartDate());
        assertEquals(endDate, foundIncome.getEndDate());
        assertEquals(user.getId(), foundIncome.getUser().getId());
    }

    @Test
    @TestTransaction
    void testFindNonStoppedRecurringIncomes_Empty() {
        // Arrange
        String uniqueName = "Test Recurring Income " + System.currentTimeMillis();
        LocalDate startDate = FIXED_DATE;
        LocalDate endDate = startDate.plusMonths(12);

        RecurringIncome recurringIncome = RecurringIncome.create(
            uniqueName,
            VALID_AMOUNT,
            VALID_CATEGORY,
            startDate,
            endDate,
            user
        );
        recurringIncome.stop(true);

        entityManager.persist(recurringIncome);

        // Act
        List<RecurringIncome> result = recurringIncomeRepository.findNonStoppedRecurringIncomes();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @TestTransaction
    void testFindNonStoppedRecurringIncomes_WithActive() {
        // Arrange
        String uniqueName1 = "Active Income " + System.currentTimeMillis();
        String uniqueName2 = "Stopped Income " + System.currentTimeMillis();
        LocalDate startDate = FIXED_DATE;
        LocalDate endDate = startDate.plusMonths(12);

        // Create active recurring income
        RecurringIncome activeIncome = RecurringIncome.create(
            uniqueName1,
            VALID_AMOUNT,
            VALID_CATEGORY,
            startDate,
            endDate,
            user
        );

        // Create stopped recurring income
        RecurringIncome stoppedIncome = RecurringIncome.create(
            uniqueName2,
            VALID_AMOUNT,
            VALID_CATEGORY,
            startDate,
            endDate,
            user
        );
        stoppedIncome.stop(true);

        // Save both
        entityManager.persist(activeIncome);
        entityManager.persist(stoppedIncome);

        // Act
        List<RecurringIncome> result = recurringIncomeRepository.findNonStoppedRecurringIncomes();

        // Assert
        assertFalse(result.isEmpty());

        // Check if our active income is in the result
        boolean containsActiveIncome = result.stream()
            .anyMatch(ri -> ri.getId().equals(activeIncome.getId()));
        assertTrue(containsActiveIncome);

        // Check that our stopped income is NOT in the result
        boolean containsStoppedIncome = result.stream()
            .anyMatch(ri -> ri.getId().equals(stoppedIncome.getId()));
        assertFalse(containsStoppedIncome);
    }

    @Test
    @TestTransaction
    void testFindNonStoppedRecurringIncomes_MultipleActive() {
        // Arrange
        LocalDate startDate = FIXED_DATE;
        LocalDate endDate = startDate.plusMonths(12);

        // Create multiple active recurring incomes
        List<RecurringIncome> createdIncomes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            RecurringIncome income = RecurringIncome.create(
                "Active Income " + i,
                VALID_AMOUNT,
                VALID_CATEGORY,
                startDate,
                endDate,
                user
            );
            entityManager.persist(income);
            createdIncomes.add(income);
        }

        // Act
        List<RecurringIncome> result = recurringIncomeRepository.findNonStoppedRecurringIncomes();

        // Assert
        assertFalse(result.isEmpty());

        // Check if all our created incomes are in the result
        Set<Long> resultIds = result.stream()
            .map(RecurringIncome::getId)
            .collect(Collectors.toSet());

        for (RecurringIncome income : createdIncomes) {
            assertTrue(resultIds.contains(income.getId()),
                "Result should contain active income with ID " + income.getId());
        }
    }
}
