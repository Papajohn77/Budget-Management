package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;

class RecurringExpenseTest {
    private static final String TEST_PASSWORD = "Test123!@#";
    private static final String VALID_NAME = "Monthly Rent";
    private static final Money VALID_AMOUNT = new Money(BigDecimal.valueOf(100));
    private static final ExpenseCategory VALID_CATEGORY = ExpenseCategory.HOUSING;
    private static final LocalDate VALID_START_DATE = LocalDate.now();
    private static final LocalDate VALID_END_DATE = LocalDate.now().plusMonths(12);
    private User user;

    @BeforeEach
    void setUp() {
        user = User.create(
                "testuser",
                "test@example.com",
                TEST_PASSWORD,
                new BCryptPasswordEncoder()
        );
    }

    @Test
    void createWithValidData() {
        // Act
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        // Assert
        assertNotNull(recurringExpense);
        assertEquals(VALID_NAME, recurringExpense.getName());
        assertEquals(VALID_AMOUNT, recurringExpense.getAmount());
        assertEquals(VALID_CATEGORY, recurringExpense.getCategory());
        assertEquals(VALID_START_DATE, recurringExpense.getStartDate());
        assertEquals(VALID_END_DATE, recurringExpense.getEndDate());
        assertEquals(user, recurringExpense.getUser());
        assertTrue(recurringExpense.getGeneratedExpenses().isEmpty());
    }

    @Test
    void createWithNullName() {
        // Act & Assert
        assertThrows(
                InvalidDomainArgumentException.class,
                () -> RecurringExpense.create(
                        null,
                        VALID_AMOUNT,
                        VALID_CATEGORY,
                        VALID_START_DATE,
                        VALID_END_DATE,
                        user
                )
        );
    }

    @Test
    void createWithEmptyName() {
        // Act & Assert
        assertThrows(
                InvalidDomainArgumentException.class,
                () -> RecurringExpense.create(
                        "   ",
                        VALID_AMOUNT,
                        VALID_CATEGORY,
                        VALID_START_DATE,
                        VALID_END_DATE,
                        user
                )
        );
    }

    @Test
    void createWithNullAmount() {
        // Act & Assert
        assertThrows(
                InvalidDomainArgumentException.class,
                () -> RecurringExpense.create(
                        VALID_NAME,
                        null,
                        VALID_CATEGORY,
                        VALID_START_DATE,
                        VALID_END_DATE,
                        user
                )
        );
    }

    @Test
    void createWithNullCategory() {
        // Act & Assert
        assertThrows(
                InvalidDomainArgumentException.class,
                () -> RecurringExpense.create(
                        VALID_NAME,
                        VALID_AMOUNT,
                        null,
                        VALID_START_DATE,
                        VALID_END_DATE,
                        user
                )
        );
    }

    @Test
    void createWithNullStartDate() {
        // Act & Assert
        assertThrows(
                InvalidDomainArgumentException.class,
                () -> RecurringExpense.create(
                        VALID_NAME,
                        VALID_AMOUNT,
                        VALID_CATEGORY,
                        null,
                        VALID_END_DATE,
                        user
                )
        );
    }

    @Test
    void createWithNullEndDate() {
        // Act & Assert
        assertThrows(
                InvalidDomainArgumentException.class,
                () -> RecurringExpense.create(
                        VALID_NAME,
                        VALID_AMOUNT,
                        VALID_CATEGORY,
                        VALID_START_DATE,
                        null,
                        user
                )
        );
    }

    @Test
    void createWithEndDateBeforeStartDate() {
        // Act & Assert
        LocalDate invalidEndDate = VALID_START_DATE.minusDays(1);

        assertThrows(
                InvalidDomainArgumentException.class,
                () -> RecurringExpense.create(
                        VALID_NAME,
                        VALID_AMOUNT,
                        VALID_CATEGORY,
                        VALID_START_DATE,
                        invalidEndDate,
                        user
                )
        );
    }

    @Test
    void createWithNullUser() {
        // Act & Assert
        assertThrows(
                InvalidDomainArgumentException.class,
                () -> RecurringExpense.create(
                        VALID_NAME,
                        VALID_AMOUNT,
                        VALID_CATEGORY,
                        VALID_START_DATE,
                        VALID_END_DATE,
                        null
                )
        );
    }

    @Test
    void stopMethodSetsIsStoppedToTrue() {
        // Arrange
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        // Act
        recurringExpense.stop(true);

        // Assert
        assertTrue(recurringExpense.isStopped());
    }

    @Test
    void stopMethodThrowsExceptionWhenStoppedIsFalse() {
        // Arrange
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        // Act & Assert
        assertThrows(
                InvalidDomainArgumentException.class,
                () -> recurringExpense.stop(false)
        );
    }

    @Test
    void testGetGeneratedExpenses() {
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        List<Expense> generatedExpenses = recurringExpense.getGeneratedExpenses();
        assertNotNull(generatedExpenses);
        assertTrue(generatedExpenses.isEmpty());

        // Verify returned list is unmodifiable
        assertThrows(
                UnsupportedOperationException.class,
                () -> generatedExpenses.add(null)
        );
    }

    @Test
    void testProtectedConstructor() {
        // This test is to cover the protected no-arg constructor
        // Since we can't directly call it, we verify that the factory method works
        // which internally uses this constructor
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        assertNotNull(recurringExpense);
    }

    @Test
    void testGetId() {
        // This is tricky to test as ID is set by JPA, but we can verify it's null before persistence
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        // ID will be null before persistence
        assertNull(recurringExpense.getId());
    }

    @Test
    void testGetLastAppliedDate() {
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        // Last applied date should be null initially
        assertNull(recurringExpense.getLastAppliedDate());
    }

    @Test
    void testIsStoppedInitialValue() {
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        // Should be false initially
        assertFalse(recurringExpense.isStopped());
    }

    @Test
    void stopMethodDoesNotChangeStateWhenExceptionThrown() {
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        // Initial state
        assertFalse(recurringExpense.isStopped());

        // Act & Assert
        try {
            recurringExpense.stop(false);
            fail("Expected InvalidDomainArgumentException was not thrown");
        } catch (InvalidDomainArgumentException e) {
            // Expected exception
        }

        // Verify state hasn't changed
        assertFalse(recurringExpense.isStopped());
    }

    @Test
    void testNotShouldApplyWhenIsStopped() {
        // Arrange
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );
        recurringExpense.stop(true);

        // Act & Assert - using reflection to access private method
        boolean result = callShouldApply(recurringExpense, LocalDate.now());
        assertFalse(result, "Stopped recurring expense should not apply");
    }

    @Test
    void testShouldNotApplyWithNullLastAppliedDateBeforeStartDate() {
        // Arrange
        LocalDate futureStartDate = LocalDate.now().plusDays(10);
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                futureStartDate,
                futureStartDate.plusMonths(12),
                user
        );

        // Act & Assert
        boolean result = callShouldApply(recurringExpense, LocalDate.now());
        assertFalse(result, "Should not apply when current date is before start date");
    }

    @Test
    void testShouldApplyWithNullLastAppliedDateAtStartDate() {
        // Arrange
        LocalDate today = LocalDate.now();
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                today,
                today.plusMonths(12),
                user
        );

        // Act & Assert
        boolean result = callShouldApply(recurringExpense, today);
        assertTrue(result, "Should apply when current date is at start date");
    }

    @Test
    void testShouldApplyWithNullLastAppliedDateAfterStartDate() {
        // Arrange
        LocalDate pastStartDate = LocalDate.now().minusDays(10);
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                pastStartDate,
                pastStartDate.plusMonths(12),
                user
        );

        // Act & Assert
        boolean result = callShouldApply(recurringExpense, LocalDate.now());
        assertTrue(result, "Should apply when current date is after start date");
    }

    @Test
    void testShouldNotApplyAfterMaximumApplicationsReached() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusMonths(4);
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                startDate,
                endDate,
                user
        );

        // Simulate having already applied 4 times without calling apply to update the values
        for (int i = 0; i < 4; i++) {
            Expense expense = Expense.create(VALID_AMOUNT, startDate.plusMonths(i), VALID_CATEGORY, user);
            addGeneratedExpense(recurringExpense, expense);
        }

        setPrivateField(recurringExpense, "lastAppliedDate", LocalDate.now());

        // Act & Assert
        boolean result = callShouldApply(recurringExpense, LocalDate.now().plusMonths(2));
        assertFalse(result, "Should not apply when maximum applications reached");
    }

    @Test
    void testShouldNotApplyLessThanOneMonthSinceLastApplied() {
        // Arrange
        LocalDate now = LocalDate.now();
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                now.minusMonths(2),
                now.plusMonths(10),
                user
        );
        setPrivateField(recurringExpense, "lastAppliedDate", now.minusDays(15));

        // Act & Assert
        boolean result = callShouldApply(recurringExpense, now);
        assertFalse(result, "Should not apply when less than one month since last applied");
    }

    @Test
    void testShouldApplyExactlyOneMonthSinceLastApplied() {
        // Arrange
        LocalDate now = LocalDate.now();
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                now.minusMonths(2),
                now.plusMonths(10),
                user
        );
        setPrivateField(recurringExpense, "lastAppliedDate", now.minusMonths(1));

        // Act & Assert
        boolean result = callShouldApply(recurringExpense, now);
        assertTrue(result, "Should apply when exactly one month since last applied");
    }

    @Test
    void testShouldApplyMoreThanOneMonthSinceLastApplied() {
        // Arrange
        LocalDate now = LocalDate.now();
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                now.minusMonths(3),
                now.plusMonths(9),
                user
        );
        setPrivateField(recurringExpense, "lastAppliedDate", now.minusMonths(2));

        // Act & Assert
        boolean result = callShouldApply(recurringExpense, now);
        assertTrue(result, "Should apply when more than one month since last applied");
    }

    @Test
    void testApplyWhenShouldNotApplyReturnsNull() {
        // Arrange
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                LocalDate.now().plusDays(10), // Future start date
                VALID_END_DATE,
                user
        );

        // Act
        Expense result = recurringExpense.apply(LocalDate.now());

        // Assert
        assertNull(result, "Apply should return null when shouldApply returns false");
    }

    @Test
    void testApplyFirstTimeCreatesExpenseWithStartDate() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(5);
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                startDate,
                startDate.plusMonths(12),
                user
        );

        // Act
        Expense result = recurringExpense.apply(LocalDate.now());

        // Assert
        assertNotNull(result);
        assertEquals(startDate, result.getDate());
        assertEquals(VALID_AMOUNT, result.getAmount());
        assertEquals(VALID_CATEGORY, result.getCategory());
        assertEquals(user, result.getUser());
        assertEquals(recurringExpense, result.getRecurringExpense());
        assertEquals(startDate, recurringExpense.getLastAppliedDate());
        assertTrue(recurringExpense.getGeneratedExpenses().contains(result));
    }

    @Test
    void testShouldApplyAccountsForPartialMonths() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(3);
        LocalDate endDate = startDate.plusMonths(2).plusDays(5);

        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                startDate,
                endDate,
                user
        );

        // Set up as if 2 applications already happened
        setPrivateField(recurringExpense, "lastAppliedDate", startDate.plusMonths(1));
        for (int i = 0; i < 2; i++) {
                Expense expense = Expense.create(VALID_AMOUNT, startDate.plusMonths(i), VALID_CATEGORY, user);
                addGeneratedExpense(recurringExpense, expense);
        }

        // Act & Assert - Third application should be allowed due to partial month
        boolean result = callShouldApply(recurringExpense, startDate.plusMonths(2));
        assertTrue(result);

        // Act & Apply the third application
        Expense thirdExpense = Expense.create(VALID_AMOUNT, startDate.plusMonths(2), VALID_CATEGORY, user);
        addGeneratedExpense(recurringExpense, thirdExpense);
        setPrivateField(recurringExpense, "lastAppliedDate", startDate.plusMonths(2));

        // Assert - Fourth application should be denied (reached maximum)
        result = callShouldApply(recurringExpense, startDate.plusMonths(3));
        assertFalse(result);
    }

    private boolean callShouldApply(RecurringExpense recurringExpense, LocalDate date) {
        try {
            Method shouldApplyMethod = RecurringExpense.class.getDeclaredMethod("shouldApply", LocalDate.class);
            shouldApplyMethod.setAccessible(true);
            return (boolean) shouldApplyMethod.invoke(recurringExpense, date);
        } catch (Exception e) {
            fail("Failed to call shouldApply method: " + e.getMessage());
            return false;
        }
    }

    private void setPrivateField(Object object, String fieldName, Object value) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (Exception e) {
            fail("Failed to set private field: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void addGeneratedExpense(RecurringExpense recurringExpense, Expense expense) {
        try {
            Class<?> clazz = recurringExpense.getClass();
            Field field = clazz.getDeclaredField("generatedExpenses");
            field.setAccessible(true);
            List<Expense> generatedExpenses = (List<Expense>) field.get(recurringExpense);
            generatedExpenses.add(expense);
        } catch (Exception e) {
            fail("Failed to add generated expense: " + e.getMessage());
        }
    }
}
