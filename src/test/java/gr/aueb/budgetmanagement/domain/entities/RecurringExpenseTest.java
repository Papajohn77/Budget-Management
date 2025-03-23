package gr.aueb.budgetmanagement.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

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

    // Tests for canBeStoppedBy method
    @Test
    void canBeStoppedByReturnsTrueForOwner() {
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
        assertTrue(recurringExpense.canBeStoppedBy(user));
    }

    @Test
    void canBeStoppedByReturnsFalseForDifferentUser() {
        // Arrange
        RecurringExpense recurringExpense = RecurringExpense.create(
                VALID_NAME,
                VALID_AMOUNT,
                VALID_CATEGORY,
                VALID_START_DATE,
                VALID_END_DATE,
                user
        );

        User anotherUser = User.create(
                "anotheruser",
                "another@example.com",
                TEST_PASSWORD,
                new BCryptPasswordEncoder()
        );

        // Act & Assert
        assertFalse(recurringExpense.canBeStoppedBy(anotherUser));
    }

    @Test
    void canBeStoppedByReturnsFalseForNullUser() {
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
        assertFalse(recurringExpense.canBeStoppedBy(null));
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
}
