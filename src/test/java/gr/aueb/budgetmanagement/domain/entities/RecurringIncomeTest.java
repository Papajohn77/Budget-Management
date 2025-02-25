package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

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

    @Nested
    class RecurringIncomeCreationTest {
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
            assertTrue(user.getRecurringIncomes().contains(recurringIncome));
        }

        @Test
        void createWithNullAmount() {
            // Act & Assert
            assertThrows(InvalidDomainArgumentException.class, () ->
                    RecurringIncome.create(
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
                    RecurringIncome.create(
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
                    RecurringIncome.create(
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
                    RecurringIncome.create(
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
                    RecurringIncome.create(
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
