package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

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

    @Nested
    class ExpenseCreationTest {
        @Test
        void createWithValidData() {
            // Act
            Expense expense = user.addExpense(
                VALID_AMOUNT,
                VALID_DATE,
                VALID_CATEGORY
            );

            // Assert
            assertNotNull(expense);
            assertEquals(VALID_AMOUNT, expense.getAmount());
            assertEquals(VALID_DATE, expense.getDate());
            assertEquals(VALID_CATEGORY, expense.getCategory());
            assertEquals(user, expense.getUser());
            assertTrue(user.getExpenses().contains(expense));
        }

        @Test
        void createWithNullAmount() {
            // Act & Assert
            assertThrows(
                InvalidDomainArgumentException.class, 
                () -> user.addExpense(null, VALID_DATE, VALID_CATEGORY)
            );
        }

        @Test
        void createWithNullDate() {
            // Act & Assert
            assertThrows(
                InvalidDomainArgumentException.class, 
                () -> user.addExpense(VALID_AMOUNT, null, VALID_CATEGORY )
            );
        }

        @Test
        void createWithNullCategory() {
            // Act & Assert
            assertThrows(
                InvalidDomainArgumentException.class, 
                () -> user.addExpense(VALID_AMOUNT, VALID_DATE, null)
            );
        }
    }
}
