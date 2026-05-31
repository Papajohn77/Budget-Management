package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.enums.SavingsOperationType;
import gr.aueb.budgetmanagement.domain.exceptions.InsufficientSavingsException;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.exceptions.SavingsAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;

class UserTest {
    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 15);
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test123!@#";
    private static final LocalDate TODAY = FIXED_DATE;

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
    void createWithNullUsername() {
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> User.create(null, TEST_EMAIL, TEST_PASSWORD, new BCryptPasswordEncoder())
        );
    }

    @Test
    void createWithEmptyUsername() {
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> User.create("   ", TEST_EMAIL, TEST_PASSWORD, new BCryptPasswordEncoder())
        );
    }

    @Test
    void shouldNotAllowMultipleSavingsAccounts() {
        Savings savings = Savings.create(user);
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

    @Test
    void addNullInvitation() {
        assertThrows(
            InvalidDomainArgumentException.class, 
            () -> user.addInvitation(null)
        );
    }

    @Test
    void getCurrentBalanceWithNoFinancialActivityShouldBeZero() {
        // Act
        BigDecimal balance = user.getCurrentBalance();
        
        // Assert
        assertEquals(BigDecimal.ZERO, balance);
    }

    @Test
    void getCurrentBalanceWithOnlyIncome() {
        // Arrange
        Money incomeAmount = new Money(new BigDecimal("500.00"));
        user.addIncome(incomeAmount, TODAY, IncomeCategory.SALARY);
        
        // Act
        BigDecimal balance = user.getCurrentBalance();
        
        // Assert
        assertEquals(incomeAmount.getValue(), balance);
    }

    @Test
    void getCurrentBalanceWithOnlyExpense() {
        // Arrange
        Money expenseAmount = new Money(new BigDecimal("300.00"));
        user.addExpense(expenseAmount, TODAY, ExpenseCategory.HOUSING);
        
        // Act
        BigDecimal balance = user.getCurrentBalance();
        
        // Assert
        assertEquals(expenseAmount.getValue().negate(), balance);
    }

    @Test
    void getCurrentBalanceWithSavingsAllocation() {
        // Arrange
        Money savingsAmount = new Money(new BigDecimal("200.00"));
        user.allocateSavings(savingsAmount, TODAY);
        
        // Act
        BigDecimal balance = user.getCurrentBalance();
        
        // Assert
        assertEquals(savingsAmount.getValue().negate(), balance);
    }

    @Test
    void getCurrentBalanceWithPiggyBankAllocation() {
        // Arrange
        Money targetAmount = new Money(new BigDecimal("1000.00"));
        PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
            "Vacation Fund",
            targetAmount,
            ExpenseCategory.ENTERTAINMENT,
            user
        );
        
        Money allocationAmount = new Money(new BigDecimal("150.00"));
        piggyBank.allocate(allocationAmount, TODAY, user);
        
        // Act
        BigDecimal balance = user.getCurrentBalance();
        
        // Assert
        assertEquals(allocationAmount.getValue().negate(), balance);
    }

    @Test
    void getCurrentBalanceWithCombinedFinancialActivity() {
        // Arrange
        // Add income
        Money incomeAmount1 = new Money(new BigDecimal("1000.00"));
        Money incomeAmount2 = new Money(new BigDecimal("500.00"));
        user.addIncome(incomeAmount1, TODAY, IncomeCategory.SALARY);
        user.addIncome(incomeAmount2, TODAY, IncomeCategory.OTHER);
        
        // Add expenses
        Money expenseAmount1 = new Money(new BigDecimal("300.00"));
        Money expenseAmount2 = new Money(new BigDecimal("150.00"));
        user.addExpense(expenseAmount1, TODAY, ExpenseCategory.FOOD);
        user.addExpense(expenseAmount2, TODAY, ExpenseCategory.HOUSING);
        
        // Allocate to savings
        Money savingsAmount = new Money(new BigDecimal("200.00"));
        user.allocateSavings(savingsAmount, TODAY);
        
        // Create and allocate to piggy bank
        PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
            "Holiday Fund",
            new Money(new BigDecimal("500.00")),
            ExpenseCategory.ENTERTAINMENT,
            user
        );
        Money piggyBankAmount = new Money(new BigDecimal("100.00"));
        piggyBank.allocate(piggyBankAmount, TODAY, user);
        
        // Calculate expected balance
        BigDecimal totalIncome = incomeAmount1.getValue().add(incomeAmount2.getValue());
        BigDecimal totalExpense = expenseAmount1.getValue().add(expenseAmount2.getValue());
        BigDecimal totalSavings = savingsAmount.getValue();
        BigDecimal totalPiggyBank = piggyBankAmount.getValue();
        BigDecimal expectedBalance = totalIncome
            .subtract(totalExpense)
            .subtract(totalSavings)
            .subtract(totalPiggyBank);
        
        // Act
        BigDecimal actualBalance = user.getCurrentBalance();
        
        // Assert
        assertEquals(expectedBalance, actualBalance);
    }

    @Test
    void equalsWithNullObject() {
        assertNotEquals(user, null);
    }

    @Test
    void equalsAndHashCode_equalUsers() {
        User user1 = User.create(
            TEST_USERNAME,
            TEST_EMAIL,
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );

        User user2 = User.create(
            TEST_USERNAME,
            TEST_EMAIL,
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void equalsAndHashCode_notEqualUsers() {
        User user1 = User.create(
            TEST_USERNAME,
            TEST_EMAIL,
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );

        User user2 = User.create(
            "validusername",
            "validemail@example.com",
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );

        assertNotEquals(user1, user2);
        assertNotEquals(user1.hashCode(), user2.hashCode());
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
