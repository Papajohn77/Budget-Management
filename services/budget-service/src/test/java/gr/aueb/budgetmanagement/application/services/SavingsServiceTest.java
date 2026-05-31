package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.commands.AllocateSavingsCommand;
import gr.aueb.budgetmanagement.application.commands.DeallocateSavingsCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.SavingsRepresentation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class SavingsServiceTest {
    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 15);
    private static final BigDecimal TEST_AMOUNT = new BigDecimal("100.00");
    private static final LocalDate TEST_DATE = FIXED_DATE;

    @Inject
    private UserRepository userRepository;
    
    @Inject
    private SavingsService savingsService;
    
    @Inject
    private SavingsOperationService savingsOperationService;
    
    private User user;
    
    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
    }
    
    @Test
    @TestTransaction
    void getSavingsForExistingUser() {
        // Act
        SavingsRepresentation result = savingsService.getSavings(user.getId());
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(user.getSavings().getId(), result.id());
        assertNotNull(result.currentAmount());
    }
    
    @Test
    @TestTransaction
    void getSavingsForNonExistentUser() {
        // Act & Assert
        assertThrows(
            NotFoundException.class,
            () -> savingsService.getSavings(999L)
        );
    }
    
    @Test
    @TestTransaction
    void getSavingsWithAllocationAndDeallocation() {
        // Get initial savings
        SavingsRepresentation initialSavings = savingsService.getSavings(user.getId());
        BigDecimal initialAmount = initialSavings.currentAmount();
        
        // Allocate funds
        AllocateSavingsCommand allocateCommand = new AllocateSavingsCommand(
            new Money(TEST_AMOUNT),
            TEST_DATE,
            user.getId()
        );
        
        savingsOperationService.allocate(allocateCommand);
        
        // Get savings after allocation
        SavingsRepresentation afterAllocationSavings = savingsService.getSavings(user.getId());
        BigDecimal afterAllocationAmount = afterAllocationSavings.currentAmount();
        
        // Verify allocation effect
        assertEquals(
            initialAmount.add(TEST_AMOUNT),
            afterAllocationAmount
        );
        
        // Deallocate half of allocated funds
        BigDecimal deallocateAmount = TEST_AMOUNT.divide(new BigDecimal("2"));
        DeallocateSavingsCommand deallocateCommand = new DeallocateSavingsCommand(
            new Money(deallocateAmount),
            TEST_DATE,
            user.getId()
        );
        
        savingsOperationService.deallocate(deallocateCommand);
        
        // Get savings after deallocation
        SavingsRepresentation finalSavings = savingsService.getSavings(user.getId());
        BigDecimal finalAmount = finalSavings.currentAmount();
        
        // Verify deallocation effect
        assertEquals(
            afterAllocationAmount.subtract(deallocateAmount),
            finalAmount
        );
    }
}
