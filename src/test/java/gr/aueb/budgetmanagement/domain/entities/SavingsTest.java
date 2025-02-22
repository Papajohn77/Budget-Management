package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.SavingsOperationType;
import gr.aueb.budgetmanagement.domain.exceptions.InsufficientSavingsException;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

class SavingsTest {
    private final LocalDate TODAY = LocalDate.now();
    private final Money AMOUNT = new Money(new BigDecimal("100.00"));

    private User user;
    private Savings savings;

    @BeforeEach
    void setUp() {
        user = User.create(
            "testuser", 
            new EmailAddress("test@example.com"), 
            "hashedPassword123"
        );
        savings = user.getSavings();
    }

    @Test
    void createForShouldCreateSavingsAndSetBidirectionalRelationship() {
        assertNotNull(savings.getUser());
        assertEquals(user, savings.getUser());
        assertEquals(savings, user.getSavings());
    }

    @Test
    void getCurrentAmountWithNoOperations() {
        assertEquals(BigDecimal.ZERO, savings.getCurrentAmount().getValue());
    }

    @Test
    void allocateShouldCreateOperation() {
        SavingsOperation operation = savings.allocate(AMOUNT, TODAY);
        
        assertNotNull(operation);
        assertEquals(AMOUNT, operation.getAmount());
        assertEquals(TODAY, operation.getDate());
        assertEquals(SavingsOperationType.ALLOCATION, operation.getOperation());
        assertEquals(savings, operation.getSavings());
        assertTrue(savings.getOperations().contains(operation));
    }

    @Test
    void allocateSavingsShouldUpdateCurrentAmount() {
        savings.allocate(AMOUNT, TODAY);
        assertEquals(AMOUNT.getValue(), savings.getCurrentAmount().getValue());
    }

    @Test
    void deallocateWithSufficientFundsShouldCreateOperation() {
        savings.allocate(AMOUNT, TODAY);
        Money deallocationAmount = new Money(
            AMOUNT.getValue().divide(new BigDecimal("2"))
        );
        
        SavingsOperation operation = savings.deallocate(deallocationAmount, TODAY);
        
        assertNotNull(operation);
        assertEquals(deallocationAmount, operation.getAmount());
        assertEquals(TODAY, operation.getDate());
        assertEquals(SavingsOperationType.DEALLOCATION, operation.getOperation());
        assertEquals(savings, operation.getSavings());
        assertTrue(savings.getOperations().contains(operation));
    }

    @Test
    void deallocateWithInsufficientFundsShouldThrowException() {
        savings.allocate(AMOUNT, TODAY);
        Money deallocationAmount = new Money(AMOUNT.getValue().add(BigDecimal.ONE));
        
        assertThrows(
            InsufficientSavingsException.class, 
            () -> savings.deallocate(deallocationAmount, TODAY)
        );
    }

    @Test
    void getCurrentAmountAfterMultipleOperationsShouldCalculateCorrectly() {
        savings.allocate(new Money(new BigDecimal("100.00")), TODAY);
        savings.deallocate(new Money(new BigDecimal("15.00")), TODAY);
        savings.allocate(new Money(new BigDecimal("50.00")), TODAY);
        savings.deallocate(new Money(new BigDecimal("30.00")), TODAY);
        
        
        assertEquals(
            new BigDecimal("105.00"), 
            savings.getCurrentAmount().getValue()
        );
    }

    @Test
    void getOperationsShouldReturnUnmodifiableList() {
        assertThrows(
            UnsupportedOperationException.class, 
            () -> savings.getOperations().add(null)
        );
    }
}
