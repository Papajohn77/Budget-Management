package gr.aueb.budgetmanagement.domain.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class RecurringIncomeTest {
    private static final String TEST_PASSWORD = "Test123!@#";
    private static final String VALID_NAME = "Monthly Salary";
    private static final Money VALID_AMOUNT = new Money(BigDecimal.valueOf(100));
    private static final IncomeCategory VALID_CATEGORY = IncomeCategory.SALARY;
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

    // Tests for canBeStoppedBy method
    @Test
    void canBeStoppedByReturnsTrueForOwner() {
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
        assertTrue(recurringIncome.canBeStoppedBy(user));
    }

    @Test
    void canBeStoppedByReturnsFalseForDifferentUser() {
        // Arrange
        RecurringIncome recurringIncome = RecurringIncome.create(
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
        assertFalse(recurringIncome.canBeStoppedBy(anotherUser));
    }

    @Test
    void canBeStoppedByReturnsFalseForNullUser() {
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
        assertFalse(recurringIncome.canBeStoppedBy(null));
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


}




