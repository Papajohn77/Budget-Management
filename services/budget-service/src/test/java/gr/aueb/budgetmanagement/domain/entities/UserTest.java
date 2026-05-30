package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.enums.SavingsOperationType;
import gr.aueb.budgetmanagement.domain.exceptions.InsufficientSavingsException;
import gr.aueb.budgetmanagement.domain.exceptions.SavingsAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;

class UserTest {
    private static final LocalDate TODAY = LocalDate.now();

    private User user;
    private Money amount;

    @BeforeEach
    void setUp() {
        user = User.create(1L);
        amount = new Money(new BigDecimal("100.00"));
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
        user.allocateSavings(amount, TODAY);
        Money deallocationAmount = new Money(
            amount.getValue().divide(new BigDecimal("2"))
        );

        SavingsOperation operation = user.deallocateSavings(deallocationAmount, TODAY);

        assertNotNull(operation);
        assertEquals(deallocationAmount, operation.getAmount());
        assertEquals(TODAY, operation.getDate());
        assertEquals(SavingsOperationType.DEALLOCATION, operation.getOperation());
        assertEquals(user.getSavings(), operation.getSavings());
    }

    @Test
    void deallocateSavingsWithInsufficientFundsShouldThrowException() {
        user.allocateSavings(amount, TODAY);
        Money largerAmount = new Money(amount.getValue().add(BigDecimal.ONE));

        assertThrows(
            InsufficientSavingsException.class,
            () -> user.deallocateSavings(largerAmount, TODAY)
        );
    }

    @Test
    void allocateSavingsShouldUpdateCurrentAmount() {
        user.allocateSavings(amount, TODAY);

        assertEquals(amount.getValue(), user.getSavings().getCurrentAmount().getValue());
    }

    @Test
    void deallocateSavingsShouldUpdateCurrentAmount() {
        user.allocateSavings(amount, TODAY);
        Money deallocationAmount = new Money(
            amount.getValue().divide(new BigDecimal("2"))
        );

        user.deallocateSavings(deallocationAmount, TODAY);

        assertEquals(
            amount.getValue().subtract(deallocationAmount.getValue()),
            user.getSavings().getCurrentAmount().getValue()
        );
    }

    @Test
    void getCurrentBalanceWithNoFinancialActivityShouldBeZero() {
        BigDecimal balance = user.getCurrentBalance();

        assertEquals(BigDecimal.ZERO, balance);
    }

    @Test
    void getCurrentBalanceWithOnlyIncome() {
        Money incomeAmount = new Money(new BigDecimal("500.00"));
        user.addIncome(incomeAmount, TODAY, IncomeCategory.SALARY);

        BigDecimal balance = user.getCurrentBalance();

        assertEquals(incomeAmount.getValue(), balance);
    }

    @Test
    void getCurrentBalanceWithOnlyExpense() {
        Money expenseAmount = new Money(new BigDecimal("300.00"));
        user.addExpense(expenseAmount, TODAY, ExpenseCategory.HOUSING);

        BigDecimal balance = user.getCurrentBalance();

        assertEquals(expenseAmount.getValue().negate(), balance);
    }

    @Test
    void getCurrentBalanceWithSavingsAllocation() {
        Money savingsAmount = new Money(new BigDecimal("200.00"));
        user.allocateSavings(savingsAmount, TODAY);

        BigDecimal balance = user.getCurrentBalance();

        assertEquals(savingsAmount.getValue().negate(), balance);
    }

    @Test
    void getCurrentBalanceWithCombinedFinancialActivity() {
        Money incomeAmount1 = new Money(new BigDecimal("1000.00"));
        Money incomeAmount2 = new Money(new BigDecimal("500.00"));
        user.addIncome(incomeAmount1, TODAY, IncomeCategory.SALARY);
        user.addIncome(incomeAmount2, TODAY, IncomeCategory.OTHER);

        Money expenseAmount1 = new Money(new BigDecimal("300.00"));
        Money expenseAmount2 = new Money(new BigDecimal("150.00"));
        user.addExpense(expenseAmount1, TODAY, ExpenseCategory.FOOD);
        user.addExpense(expenseAmount2, TODAY, ExpenseCategory.HOUSING);

        Money savingsAmount = new Money(new BigDecimal("200.00"));
        user.allocateSavings(savingsAmount, TODAY);

        BigDecimal totalIncome = incomeAmount1.getValue().add(incomeAmount2.getValue());
        BigDecimal totalExpense = expenseAmount1.getValue().add(expenseAmount2.getValue());
        BigDecimal expectedBalance = totalIncome
            .subtract(totalExpense)
            .subtract(savingsAmount.getValue());

        assertEquals(expectedBalance, user.getCurrentBalance());
    }

    @Test
    void equalsWithNullObject() {
        assertNotEquals(user, null);
    }
}
