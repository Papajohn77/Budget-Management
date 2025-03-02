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

class PiggyBankAllocationTest {
    private static final String TEST_PASSWORD = "Test123!@#";
    private static final Money VALID_AMOUNT = new Money(BigDecimal.valueOf(100));
    private static final LocalDate VALID_DATE = LocalDate.now();

    private User user;
    private PiggyBank piggyBank;

    @BeforeEach
    void setUp() {
        user = User.create(
            "testuser",
            "test@example.com",
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );
        piggyBank = PersonalPiggyBank.create(
            "Test PiggyBank",
            new Money(new BigDecimal("1000.00")),
            ExpenseCategory.OTHER,
            user
        );
    }

    @Test
    void createWithValidDataShouldCreateAllocation() {
        // Act
        PiggyBankAllocation allocation = PiggyBankAllocation.create(
            VALID_AMOUNT,
            VALID_DATE,
            piggyBank,
            user
        );

        // Assert
        assertNotNull(allocation);
        assertEquals(VALID_AMOUNT, allocation.getAmount());
        assertEquals(VALID_DATE, allocation.getDate());
        assertEquals(piggyBank, allocation.getPiggyBank());
        assertEquals(user, allocation.getUser());
    }

    @Test
    void createWithNullAmountShouldThrowException() {
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> PiggyBankAllocation.create(
                null,
                VALID_DATE,
                piggyBank,
                user
            )
        );
    }

    @Test
    void createWithNullDateShouldThrowException() {
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> PiggyBankAllocation.create(
                VALID_AMOUNT,
                null,
                piggyBank,
                user
            )
        );
    }

    @Test
    void createWithNullPiggyBankShouldThrowException() {
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> PiggyBankAllocation.create(
                VALID_AMOUNT,
                VALID_DATE,
                null,
                user
            )
        );
    }

    @Test
    void createWithNullUserShouldThrowException() {
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> PiggyBankAllocation.create(
                VALID_AMOUNT,
                VALID_DATE,
                piggyBank,
                null
            )
        );
    }
}
