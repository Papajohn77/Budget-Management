package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.enums.SavingsOperationType;
import gr.aueb.budgetmanagement.domain.exceptions.InsufficientSavingsException;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.exceptions.SavingsAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;

class UserTest {
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test123!@#";
    private static final LocalDate TODAY = LocalDate.now();

    private User user;
    private Money amount;

    @BeforeEach
    void setUp() {
        user = User.create(
            TEST_USERNAME,
            TEST_EMAIL,
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );
        amount = new Money(new BigDecimal("100.00"));
    }

    @Test
    void shouldNotAllowMultipleSavingsAccounts() {
        Savings savings = Savings.createFor(user);
        assertThrows(
            SavingsAlreadyExistsException.class, 
            () -> user.setSavings(savings)
        );
    }

    @Test
    void allocateSavingsShouldCreateOperation() {
        SavingsOperation operation = user.allocateSavings(amount, TODAY);

        assertNotNull(operation);
        assertEquals(amount, operation.getAmount());
        assertEquals(TODAY, operation.getDate());
        assertEquals(SavingsOperationType.ALLOCATION, operation.getOperation());
        assertEquals(user.getSavings(), operation.getSavings());
    }

    @Test
    void deallocateSavingsWithSufficientFundsShouldCreateOperation() {
        // Arrange
        user.allocateSavings(amount, TODAY);
        Money deallocationAmount = new Money(
            amount.getValue().divide(new BigDecimal("2"))
        );

        // Act
        SavingsOperation operation = user.deallocateSavings(deallocationAmount, TODAY);

        // Assert
        assertNotNull(operation);
        assertEquals(deallocationAmount, operation.getAmount());
        assertEquals(TODAY, operation.getDate());
        assertEquals(SavingsOperationType.DEALLOCATION, operation.getOperation());
        assertEquals(user.getSavings(), operation.getSavings());
    }

    @Test
    void deallocateSavingsWithInsufficientFundsShouldThrowException() {
        // Arrange
        user.allocateSavings(amount, TODAY);
        Money largerAmount = new Money(amount.getValue().add(BigDecimal.ONE));

        // Act & Assert
        assertThrows(
            InsufficientSavingsException.class,
            () -> user.deallocateSavings(largerAmount, TODAY)
        );
    }

    @Test
    void allocateSavingsShouldUpdateCurrentAmount() {
        // Act
        user.allocateSavings(amount, TODAY);

        // Assert
        assertEquals(amount.getValue(), user.getSavings().getCurrentAmount().getValue());
    }

    @Test
    void deallocateSavingsShouldUpdateCurrentAmount() {
        // Arrange
        user.allocateSavings(amount, TODAY);
        Money deallocationAmount = new Money(
            amount.getValue().divide(new BigDecimal("2"))
        );

        // Act
        user.deallocateSavings(deallocationAmount, TODAY);

        // Assert
        assertEquals(
            amount.getValue().subtract(deallocationAmount.getValue()), 
            user.getSavings().getCurrentAmount().getValue()
        );
    }

    @Test
    void addPiggyBankShouldAddToUser() {
        PersonalPiggyBank piggyBank = createPersonalPiggyBank(user);

        assertTrue(user.getPiggyBanks().contains(piggyBank));
        assertEquals(1, user.getPiggyBanks().size());
    }

    @Test
    void addPiggyBankWithNullPiggyBank() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> user.addPiggyBank(null)
        );
    }

    @Test
    void addPiggyBankTwiceShouldNotDuplicate() {
        // Arrange
        PersonalPiggyBank piggyBank = createPersonalPiggyBank(user);

        // Act
        user.addPiggyBank(piggyBank); // Adding again

        // Assert
        assertEquals(1, user.getPiggyBanks().size());
    }

    @Test
    void getPiggyBanksShouldReturnUnmodifiableSet() {
        // Arrange
        Set<PersonalPiggyBank> piggyBanks = user.getPiggyBanks();

        // Act & Assert
        assertThrows(
            UnsupportedOperationException.class,
            () -> piggyBanks.add(new PersonalPiggyBank())
        );
    }

    private PersonalPiggyBank createPersonalPiggyBank(User user) {
        return PersonalPiggyBank.create(
            "Test Piggy Bank",
            new Money(new BigDecimal("100.00")),
            ExpenseCategory.OTHER,
            user
        );
    }
}
