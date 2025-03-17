package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    private static final ExpenseCategory VALID_CATEGORY = ExpenseCategory.FOOD;
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
}
