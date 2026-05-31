package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

class IncomeTest {
    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 15);
    private static final Money VALID_AMOUNT = new Money(BigDecimal.valueOf(100));
    private static final IncomeCategory VALID_CATEGORY = IncomeCategory.SALARY;
    private static final LocalDate VALID_DATE = FIXED_DATE;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.create(1L);
    }

    @Test
    void createWithValidData() {
        // Act
        Income income = Income.create(
            VALID_AMOUNT,
            VALID_DATE,
            VALID_CATEGORY,
            user
        );

        // Assert
        assertNotNull(income);
        assertEquals(VALID_AMOUNT, income.getAmount());
        assertEquals(VALID_DATE, income.getDate());
        assertEquals(VALID_CATEGORY, income.getCategory());
        assertEquals(user, income.getUser());
    }

    @Test
    void createWithNullAmount() {
        // Act & Assert
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> Income.create(null, VALID_DATE, VALID_CATEGORY, user)
        );
    }

    @Test
    void createWithNullDate() {
        // Act & Assert
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> Income.create(VALID_AMOUNT, null, VALID_CATEGORY, user)
        );
    }

    @Test
    void createWithNullCategory() {
        // Act & Assert
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> Income.create(VALID_AMOUNT, VALID_DATE, null, user)
        );
    }

    @Test
    void createWithNullUser() {
        // Act & Assert
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> Income.create(VALID_AMOUNT, VALID_DATE, VALID_CATEGORY, null)
        );
    }

    @Test
    void testAddRecurringExpense() {
        // Arrange
        Income income = Income.create(
            VALID_AMOUNT,
            VALID_DATE,
            VALID_CATEGORY,
            user
        );

        RecurringIncome recurringIncome = RecurringIncome.create(
            "Monthly Garage Rent",
            VALID_AMOUNT,
            VALID_CATEGORY,
            FIXED_DATE,
            FIXED_DATE.plusMonths(12),
            user
        );

        // Act
        income.addRecurringIncome(recurringIncome);

        // Assert
        assertEquals(recurringIncome, income.getRecurringIncome());
    }

    @Test
    void testBalanceImpactImplementation() {
        // Arrange
        BigDecimal amount = new BigDecimal("150.00");
        Income expense = Income.create(
            new Money(amount),
            VALID_DATE,
            VALID_CATEGORY,
            user
        );
        
        // Act
        BigDecimal impact = expense.applyToBalance();
        
        // Assert
        assertEquals(amount, impact);
    }
}
