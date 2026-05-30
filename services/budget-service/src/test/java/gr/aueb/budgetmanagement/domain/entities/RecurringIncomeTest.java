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

import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

class RecurringIncomeTest {
    private static final String VALID_NAME = "Monthly Salary";
    private static final Money VALID_AMOUNT = new Money(BigDecimal.valueOf(100));
    private static final IncomeCategory VALID_CATEGORY = IncomeCategory.SALARY;
    private static final LocalDate VALID_START_DATE = LocalDate.now();
    private static final LocalDate VALID_END_DATE = LocalDate.now().plusMonths(12);
    private User user;

    @BeforeEach
    void setUp() {
        user = User.create(1L);
    }

    @Test
    void createWithValidData() {
        // Act
        RecurringIncome recurringIncome = RecurringIncome.create(
            VALID_NAME,
            VALID_AMOUNT,
            VALID_CATEGORY,
            VALID_START_DATE,
            VALID_END_DATE,
            user
        );

        // Assert
        assertNotNull(recurringIncome);
        assertEquals(VALID_NAME, recurringIncome.getName());
        assertEquals(VALID_AMOUNT, recurringIncome.getAmount());
        assertEquals(VALID_CATEGORY, recurringIncome.getCategory());
        assertEquals(VALID_START_DATE, recurringIncome.getStartDate());
        assertEquals(VALID_END_DATE, recurringIncome.getEndDate());
        assertEquals(user, recurringIncome.getUser());
        assertTrue(recurringIncome.getGeneratedIncomes().isEmpty());
    }

    @Test
    void createWithNullName() {
        // Act & Assert
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> RecurringIncome.create(
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
            () -> RecurringIncome.create(
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
            () -> RecurringIncome.create(
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
            () -> RecurringIncome.create(
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
            () -> RecurringIncome.create(
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
            () -> RecurringIncome.create(
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
            () -> RecurringIncome.create(
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
            () -> RecurringIncome.create(
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
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        // Act
        recurringIncome.stop(true);

        // Assert
        assertTrue(recurringIncome.isStopped());
    }

    @Test
    void stopMethodThrowsExceptionWhenStoppedIsFalse() {
        // Arrange
        RecurringIncome recurringIncome = RecurringIncome.create(
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
                () -> recurringIncome.stop(false)
        );
    }

    @Test
    void testGetGeneratedIncomes() {
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        List<Income> generatedIncomes = recurringIncome.getGeneratedIncomes();
        assertNotNull(generatedIncomes);
        assertTrue(generatedIncomes.isEmpty());

        // Verify returned list is unmodifiable
        assertThrows(
                UnsupportedOperationException.class,
                () -> generatedIncomes.add(null)
        );
    }

    @Test
    void testProtectedConstructor() {
        // This test is to cover the protected no-arg constructor
        // Since we can't directly call it, we verify that the factory method works
        // which internally uses this constructor
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        assertNotNull(recurringIncome);
    }

    @Test
    void testGetId() {
        // This is tricky to test as ID is set by JPA, but we can verify it's null before persistence
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        // ID will be null before persistence
        assertNull(recurringIncome.getId());
    }

    @Test
    void testGetLastAppliedDate() {
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        // Last applied date should be null initially
        assertNull(recurringIncome.getLastAppliedDate());
    }

    @Test
    void testIsStoppedInitialValue() {
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        // Should be false initially
        assertFalse(recurringIncome.isStopped());
    }

    @Test
    void stopMethodDoesNotChangeStateWhenExceptionThrown() {
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        // Initial state
        assertFalse(recurringIncome.isStopped());

        // Act & Assert
        try {
            recurringIncome.stop(false);
            fail("Expected InvalidDomainArgumentException was not thrown");
        } catch (InvalidDomainArgumentException e) {
            // Expected exception
        }

        // Verify state hasn't changed
        assertFalse(recurringIncome.isStopped());
    }

    @Test
    void testNotShouldApplyWhenIsStopped() {
        // Arrange
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );
        recurringIncome.stop(true);
        
        // Act & Assert - using reflection to access private method
        boolean result = callShouldApply(recurringIncome, LocalDate.now());
        assertFalse(result, "Stopped recurring income should not apply");
    }

    @Test
    void testShouldNotApplyWithNullLastAppliedDateBeforeStartDate() {
        // Arrange
        LocalDate futureStartDate = LocalDate.now().plusDays(10);
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                futureStartDate,
                futureStartDate.plusMonths(12),
                user
        );
        
        // Act & Assert
        boolean result = callShouldApply(recurringIncome, LocalDate.now());
        assertFalse(result, "Should not apply when current date is before start date");
    }

    @Test
    void testShouldApplyWithNullLastAppliedDateAtStartDate() {
        // Arrange
        LocalDate today = LocalDate.now();
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                today,
                today.plusMonths(12),
                user
        );
        
        // Act & Assert
        boolean result = callShouldApply(recurringIncome, today);
        assertTrue(result, "Should apply when current date is at start date");
    }

    @Test
    void testShouldApplyWithNullLastAppliedDateAfterStartDate() {
        // Arrange
        LocalDate pastStartDate = LocalDate.now().minusDays(10);
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                pastStartDate,
                pastStartDate.plusMonths(12),
                user
        );
        
        // Act & Assert
        boolean result = callShouldApply(recurringIncome, LocalDate.now());
        assertTrue(result, "Should apply when current date is after start date");
    }

    @Test
    void testShouldNotApplyAfterMaximumApplicationsReached() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusMonths(4);
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                startDate,
                endDate,
                user
        );
        
        // Simulate having already applied 4 times without calling apply to update the values
        for (int i = 0; i < 4; i++) {
            Income income = Income.create(VALID_AMOUNT, startDate.plusMonths(i), VALID_CATEGORY, user);
            addGeneratedIncome(recurringIncome, income);
        }
        setPrivateField(recurringIncome, "lastAppliedDate", LocalDate.now());
        
        // Act & Assert
        boolean result = callShouldApply(recurringIncome, LocalDate.now().plusMonths(2));
        assertFalse(result, "Should not apply when maximum applications reached");
    }

    @Test
    void testShouldNotApplyLessThanOneMonthSinceLastApplied() {
        // Arrange
        LocalDate now = LocalDate.now();
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                now.minusMonths(2),
                now.plusMonths(10),
                user
        );
        setPrivateField(recurringIncome, "lastAppliedDate", now.minusDays(15));
        
        // Act & Assert
        boolean result = callShouldApply(recurringIncome, now);
        assertFalse(result, "Should not apply when less than one month since last applied");
    }

    @Test
    void testShouldApplyExactlyOneMonthSinceLastApplied() {
        // Arrange
        LocalDate now = LocalDate.now();
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                now.minusMonths(2),
                now.plusMonths(10),
                user
        );
        setPrivateField(recurringIncome, "lastAppliedDate", now.minusMonths(1));
        
        // Act & Assert
        boolean result = callShouldApply(recurringIncome, now);
        assertTrue(result, "Should apply when exactly one month since last applied");
    }

    @Test
    void testShouldApplyMoreThanOneMonthSinceLastApplied() {
        // Arrange
        LocalDate now = LocalDate.now();
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                now.minusMonths(3),
                now.plusMonths(9),
                user
        );
        setPrivateField(recurringIncome, "lastAppliedDate", now.minusMonths(2));
        
        // Act & Assert
        boolean result = callShouldApply(recurringIncome, now);
        assertTrue(result, "Should apply when more than one month since last applied");
    }

    @Test
    void testApplyWhenShouldNotApplyReturnsNull() {
        // Arrange
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                LocalDate.now().plusDays(10), // Future start date
                VALID_END_DATE,
                user
        );
        
        // Act
        Income result = recurringIncome.apply(LocalDate.now());
        
        // Assert
        assertNull(result, "Apply should return null when shouldApply returns false");
    }

    @Test
    void testApplyFirstTimeCreatesIncomeWithStartDate() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusDays(5);
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                startDate,
                startDate.plusMonths(12),
                user
        );
        
        // Act
        Income result = recurringIncome.apply(LocalDate.now());
        
        // Assert
        assertNotNull(result);
        assertEquals(startDate, result.getDate());
        assertEquals(VALID_AMOUNT, result.getAmount());
        assertEquals(VALID_CATEGORY, result.getCategory());
        assertEquals(user, result.getUser());
        assertEquals(recurringIncome, result.getRecurringIncome());
        assertEquals(startDate, recurringIncome.getLastAppliedDate());
        assertTrue(recurringIncome.getGeneratedIncomes().contains(result));
    }

    @Test
    void testShouldApplyAccountsForPartialMonths() {
        // Arrange
        LocalDate startDate = LocalDate.now().minusMonths(3);
        LocalDate endDate = startDate.plusMonths(2).plusDays(5);
        
        RecurringIncome recurringIncome = RecurringIncome.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                startDate,
                endDate,
                user
        );
        
        // Set up as if 2 applications already happened
        setPrivateField(recurringIncome, "lastAppliedDate", startDate.plusMonths(1));
        for (int i = 0; i < 2; i++) {
                Income income = Income.create(VALID_AMOUNT, startDate.plusMonths(i), VALID_CATEGORY, user);
                addGeneratedIncome(recurringIncome, income);
        }
        
        // Act & Assert - Third application should be allowed due to partial month
        boolean result = callShouldApply(recurringIncome, startDate.plusMonths(2));
        assertTrue(result);
        
        // Act & Apply the third application
        Income thirdIncome = Income.create(VALID_AMOUNT, startDate.plusMonths(2), VALID_CATEGORY, user);
        addGeneratedIncome(recurringIncome, thirdIncome);
        setPrivateField(recurringIncome, "lastAppliedDate", startDate.plusMonths(2));
        
        // Assert - Fourth application should be denied (reached maximum)
        result = callShouldApply(recurringIncome, startDate.plusMonths(3));
        assertFalse(result);
    }

    private boolean callShouldApply(RecurringIncome recurringIncome, LocalDate date) {
        try {
            Method shouldApplyMethod = RecurringIncome.class.getDeclaredMethod("shouldApply", LocalDate.class);
            shouldApplyMethod.setAccessible(true);
            return (boolean) shouldApplyMethod.invoke(recurringIncome, date);
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
    private void addGeneratedIncome(RecurringIncome recurringIncome, Income income) {
        try {
            Class<?> clazz = recurringIncome.getClass();
            Field field = clazz.getDeclaredField("generatedIncomes");
            field.setAccessible(true);
            List<Income> generatedIncomes = (List<Income>) field.get(recurringIncome);
            generatedIncomes.add(income);
        } catch (Exception e) {
            fail("Failed to add generated income: " + e.getMessage());
        }
    }
}




