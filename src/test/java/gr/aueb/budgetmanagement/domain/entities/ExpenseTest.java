package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;

class ExpenseTest {
    private static final String TEST_PASSWORD = "Test123!@#";
    private static final Money VALID_AMOUNT = new Money(BigDecimal.valueOf(100));
    private static final ExpenseCategory VALID_CATEGORY = ExpenseCategory.FOOD;
    private static final LocalDate VALID_DATE = LocalDate.now();
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
        Expense expense = Expense.create(
            VALID_AMOUNT,
            VALID_DATE,
            VALID_CATEGORY,
            user
        );

        // Assert
        assertNotNull(expense);
        assertEquals(VALID_AMOUNT, expense.getAmount());
        assertEquals(VALID_DATE, expense.getDate());
        assertEquals(VALID_CATEGORY, expense.getCategory());
        assertEquals(user, expense.getUser());
    }

    @Test
    void createWithNullAmount() {
        // Act & Assert
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> Expense.create(null, VALID_DATE, VALID_CATEGORY, user)
        );
    }

    @Test
    void createWithNullDate() {
        // Act & Assert
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> Expense.create(VALID_AMOUNT, null, VALID_CATEGORY, user)
        );
    }

    @Test
    void createWithNullCategory() {
        // Act & Assert
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> Expense.create(VALID_AMOUNT, VALID_DATE, null, user)
        );
    }

    @Test
    void createWithNullUser() {
        // Act & Assert
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> Expense.create(VALID_AMOUNT, VALID_DATE, VALID_CATEGORY, null)
        );
    }

    @Test
    void testAddRecurringExpense() {
        // Arrange
        Expense expense = Expense.create(
            VALID_AMOUNT,
            VALID_DATE,
            VALID_CATEGORY,
            user
        );

        RecurringExpense recurringExpense = RecurringExpense.create(
            "Monthly Subscription",
            VALID_AMOUNT,
            VALID_CATEGORY,
            LocalDate.now(),
            LocalDate.now().plusMonths(12),
            user
        );

        // Act
        expense.addRecurringExpense(recurringExpense);

        // Assert
        assertEquals(recurringExpense, expense.getRecurringExpense());
    }

    @Test
    void testBalanceImpactImplementation() {
        // Arrange
        BigDecimal amount = new BigDecimal("150.00");
        Expense expense = Expense.create(
            new Money(amount),
            VALID_DATE,
            VALID_CATEGORY,
            user
        );
        
        // Act
        BigDecimal impact = expense.applyToBalance();
        
        // Assert
        assertEquals(amount.negate(), impact);
    }
}
