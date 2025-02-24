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
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

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

    @Nested
    class RecurringExpenseCreationTest {
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
            assertTrue(user.getRecurringExpenses().contains(recurringExpense));
        }

        @Test
        void createWithNullAmount() {
            // Act & Assert
            assertThrows(InvalidDomainArgumentException.class, () ->
                    RecurringExpense.create(
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
            assertThrows(InvalidDomainArgumentException.class, () ->
                    RecurringExpense.create(
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
            assertThrows(InvalidDomainArgumentException.class, () ->
                    RecurringExpense.create(
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
            assertThrows(InvalidDomainArgumentException.class, () ->
                    RecurringExpense.create(
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

            assertThrows(IllegalArgumentException.class, () ->
                    RecurringExpense.create(
                            VALID_NAME,
                            VALID_AMOUNT,
                            VALID_CATEGORY,
                            VALID_START_DATE,
                            invalidEndDate,
                            user
                    )
            );
        }
    }
}