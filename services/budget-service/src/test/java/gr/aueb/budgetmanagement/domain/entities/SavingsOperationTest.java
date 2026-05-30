package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.SavingsOperationType;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

class SavingsOperationTest {
    private final LocalDate TODAY = LocalDate.now();
    private final Money AMOUNT = new Money(new BigDecimal("100.00"));

    private User user;
    private Savings savings;

    @BeforeEach
    void setUp() {
        user = User.create(1L);
        savings = user.getSavings();
    }

    @Test
    void createWithValidDataShouldCreateOperation() {
        SavingsOperation operation = SavingsOperation.create(
            AMOUNT, TODAY, SavingsOperationType.ALLOCATION, savings);
        
        assertNotNull(operation);
        assertEquals(AMOUNT, operation.getAmount());
        assertEquals(TODAY, operation.getDate());
        assertEquals(SavingsOperationType.ALLOCATION, operation.getOperation());
        assertEquals(savings, operation.getSavings());
        assertEquals(user, operation.getUser());
    }

    @Test
    void createWithNullAmountShouldThrowException() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> SavingsOperation.create(null, TODAY, SavingsOperationType.ALLOCATION, savings)
        );
    }

    @Test
    void createWithNullDateShouldThrowException() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> SavingsOperation.create(AMOUNT, null, SavingsOperationType.ALLOCATION, savings)
        );
    }

    @Test
    void createWithNullOperationShouldThrowException() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> SavingsOperation.create(AMOUNT, TODAY, null, savings)
        );
    }

    @Test
    void createWithNullSavingsShouldThrowException() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> SavingsOperation.create(AMOUNT, TODAY, SavingsOperationType.ALLOCATION, null)
        );
    }
}
