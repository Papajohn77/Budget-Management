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
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

class IncomeTest {
    private static final String TEST_PASSWORD = "Test123!@#";
    private static final Money VALID_AMOUNT = new Money(BigDecimal.valueOf(100));
    private static final IncomeCategory VALID_CATEGORY = IncomeCategory.SALARY;
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
    class IncomeCreationTest {
        @Test
        void createWithValidData() {
            // Act
            Income income = user.addIncome(
                VALID_AMOUNT,
                VALID_DATE,
                VALID_CATEGORY
            );

            // Assert
            assertNotNull(income);
            assertEquals(VALID_AMOUNT, income.getAmount());
            assertEquals(VALID_DATE, income.getDate());
            assertEquals(VALID_CATEGORY, income.getCategory());
            assertEquals(user, income.getUser());
            assertTrue(user.getIncomes().contains(income));
        }

        @Test
        void createWithNullAmount() {
            // Act & Assert
            assertThrows(
                InvalidDomainArgumentException.class, 
                () -> user.addIncome(null, VALID_DATE, VALID_CATEGORY)
            );
        }

        @Test
        void createWithNullDate() {
            // Act & Assert
            assertThrows(
                InvalidDomainArgumentException.class, 
                () -> user.addIncome(VALID_AMOUNT, null, VALID_CATEGORY)
            );
        }

        @Test
        void createWithNullCategory() {
            // Act & Assert
            assertThrows(
                InvalidDomainArgumentException.class, () ->
                user.addIncome(VALID_AMOUNT, VALID_DATE, null)
            );
        }
    }
}
