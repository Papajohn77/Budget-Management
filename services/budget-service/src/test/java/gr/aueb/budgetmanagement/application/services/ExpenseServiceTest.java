package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.commands.AddExpenseCommand;
import gr.aueb.budgetmanagement.application.commands.UpdateExpenseCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.ExpenseRepresentation;
import gr.aueb.budgetmanagement.application.representations.ExpensesRepresentation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class ExpenseServiceTest {
    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 15);
    private static final BigDecimal EXPENSE_AMOUNT = new BigDecimal("150.00");
    private static final LocalDate EXPENSE_DATE = FIXED_DATE;

    @Inject
    private UserRepository userRepository;

    @Inject
    private ExpenseService expenseService;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
    }

    @Test
    @TestTransaction
    void testCreateExpense() {
        AddExpenseCommand command = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.FOOD,
                EXPENSE_DATE,
                user.getId()
        );

        var result = expenseService.createExpense(command);

        assertNotNull(result.id());
        assertEquals(EXPENSE_AMOUNT, result.amount());
        assertEquals(EXPENSE_DATE, result.date());
        assertEquals(ExpenseCategory.FOOD, result.category());

        User persistedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(1, persistedUser.getExpenses().size());
    }

    @Test
    @TestTransaction
    void testCreateExpenseUserNotFound() {
        AddExpenseCommand command = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.FOOD,
                EXPENSE_DATE,
                999L
        );

        assertThrows(
                NotFoundException.class,
                () -> expenseService.createExpense(command)
        );
    }

    @Test
    @TestTransaction
    void testCreateExpenseWithPastDate() {
        LocalDate pastDate = EXPENSE_DATE.minusDays(5);
        AddExpenseCommand command = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.HEALTH,
                pastDate,
                user.getId()
        );

        var result = expenseService.createExpense(command);

        assertNotNull(result.id());
        assertEquals(EXPENSE_AMOUNT, result.amount());
        assertEquals(pastDate, result.date());
        assertEquals(ExpenseCategory.HEALTH, result.category());
    }


    @Test
    @TestTransaction
    void testUpdateExpenseNotFound() {
        UpdateExpenseCommand command = new UpdateExpenseCommand(
                999L,
                user.getId(),
                new Money(EXPENSE_AMOUNT),
                EXPENSE_DATE,
                ExpenseCategory.FOOD
        );

        assertThrows(
                NotFoundException.class,
                () -> expenseService.updateExpense(command)
        );
    }

    @Test
    @TestTransaction
    void testDeleteExpense() {
        // First create an expense
        AddExpenseCommand createCommand = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.FOOD,
                EXPENSE_DATE,
                user.getId()
        );

        var createdExpense = expenseService.createExpense(createCommand);

        // Then delete it
        expenseService.deleteExpense(createdExpense.id(), user.getId());

        // Verify it's deleted
        User persistedUser = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(persistedUser.getExpenses().isEmpty());
    }

    @Test
    @TestTransaction
    void testDeleteExpenseUserNotFound() {
        assertThrows(
                NotFoundException.class,
                () -> expenseService.deleteExpense(1L, 999L)
        );
    }

    @Test
    @TestTransaction
    void testGetExpensesWithNoFilters() {
        // Create a couple of expenses
        AddExpenseCommand command1 = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.FOOD,
                EXPENSE_DATE,
                user.getId()
        );

        AddExpenseCommand command2 = new AddExpenseCommand(
                new Money(new BigDecimal("200.00")),
                ExpenseCategory.TRANSPORTATION,
                EXPENSE_DATE.minusDays(1),
                user.getId()
        );

        expenseService.createExpense(command1);
        expenseService.createExpense(command2);

        // Get all expenses (no filters)
        ExpensesRepresentation result = expenseService.getExpenses(user.getId(), null, null, null);

        assertEquals(2, result.expenses().size());
    }

    @Test
    @TestTransaction
    void testGetExpensesUserNotFound() {
        assertThrows(
                NotFoundException.class,
                () -> expenseService.getExpenses(999L, null, null, null)
        );
    }

    @Test
    @TestTransaction
    void testGetExpensesWithDateFilters() {
        // Create expenses with different dates
        LocalDate today = FIXED_DATE;
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);

        AddExpenseCommand command1 = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.FOOD,
                today,
                user.getId()
        );

        AddExpenseCommand command2 = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.TRANSPORTATION,
                yesterday,
                user.getId()
        );

        AddExpenseCommand command3 = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.HEALTH,
                lastWeek,
                user.getId()
        );

        expenseService.createExpense(command1);
        expenseService.createExpense(command2);
        expenseService.createExpense(command3);

        // Test fromDate filter
        ExpensesRepresentation resultFromDate = expenseService.getExpenses(
                user.getId(), yesterday, null, null);
        assertEquals(2, resultFromDate.expenses().size());

        // Test toDate filter
        ExpensesRepresentation resultToDate = expenseService.getExpenses(
                user.getId(), null, yesterday, null);
        assertEquals(2, resultToDate.expenses().size());

        // Test both date filters
        ExpensesRepresentation resultBothDates = expenseService.getExpenses(
                user.getId(), yesterday, today, null);
        assertEquals(2, resultBothDates.expenses().size());
    }

    @Test
    @TestTransaction
    void testGetExpensesWithCategoryFilter() {
        // Create expenses with different categories
        AddExpenseCommand command1 = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.FOOD,
                EXPENSE_DATE,
                user.getId()
        );

        AddExpenseCommand command2 = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.TRANSPORTATION,
                EXPENSE_DATE,
                user.getId()
        );

        AddExpenseCommand command3 = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.FOOD,
                EXPENSE_DATE,
                user.getId()
        );

        expenseService.createExpense(command1);
        expenseService.createExpense(command2);
        expenseService.createExpense(command3);

        // Test category filter
        ExpensesRepresentation result = expenseService.getExpenses(
                user.getId(), null, null, ExpenseCategory.FOOD);

        assertEquals(2, result.expenses().size());
        for (ExpenseRepresentation expense : result.expenses()) {
            assertEquals(ExpenseCategory.FOOD, expense.category());
        }
    }

    @Test
    @TestTransaction
    void testGetExpensesWithAllFilters() {
        // Create expenses with different dates and categories
        LocalDate today = FIXED_DATE;
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);

        AddExpenseCommand command1 = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.FOOD,
                today,
                user.getId()
        );

        AddExpenseCommand command2 = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.TRANSPORTATION,
                yesterday,
                user.getId()
        );

        AddExpenseCommand command3 = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.FOOD,
                lastWeek,
                user.getId()
        );

        expenseService.createExpense(command1);
        expenseService.createExpense(command2);
        expenseService.createExpense(command3);

        // Test with all filters
        ExpensesRepresentation result = expenseService.getExpenses(
                user.getId(), yesterday, today, ExpenseCategory.FOOD);

        assertEquals(1, result.expenses().size());
        assertEquals(ExpenseCategory.FOOD, result.expenses().get(0).category());
    }

    @Test
    @TestTransaction
    void testUpdateExpense() {
        // First create an expense
        AddExpenseCommand createCommand = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.OTHER,
                EXPENSE_DATE,
                user.getId()
        );

        ExpenseRepresentation createdExpense = expenseService.createExpense(createCommand);

        // Now update it
        BigDecimal updatedAmount = new BigDecimal("200.00");
        LocalDate updatedDate = EXPENSE_DATE.plusDays(1);
        ExpenseCategory updatedCategory = ExpenseCategory.HOUSING;

        UpdateExpenseCommand updateCommand = new UpdateExpenseCommand(
                createdExpense.id(),
                user.getId(),
                new Money(updatedAmount),
                updatedDate,
                updatedCategory
        );

        ExpenseRepresentation updatedExpense = expenseService.updateExpense(updateCommand);

        assertNotNull(updatedExpense);
        assertEquals(createdExpense.id(), updatedExpense.id());
        assertEquals(updatedAmount, updatedExpense.amount());
        assertEquals(updatedDate, updatedExpense.date());
        assertEquals(updatedCategory, updatedExpense.category());

        User persistedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(1, persistedUser.getExpenses().size());
    }

    @Test
    @TestTransaction
    void testUpdateExpenseUserNotFound() {
        // First create an expense with the valid user
        AddExpenseCommand createCommand = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.HOUSING,
                EXPENSE_DATE,
                user.getId()
        );

        ExpenseRepresentation createdExpense = expenseService.createExpense(createCommand);

        // Now try to update with a non-existent user ID (999L)
        UpdateExpenseCommand updateCommand = new UpdateExpenseCommand(
                createdExpense.id(),
                999L, // Non-existent user ID
                new Money(new BigDecimal("200.00")),
                EXPENSE_DATE,
                ExpenseCategory.HOUSING
        );

        assertThrows(
                NotFoundException.class,
                () -> expenseService.updateExpense(updateCommand)
        );
    }

    @Test
    @TestTransaction
    void testGetExpensesWithNoExpenses() {
        // User exists but has no expenses
        ExpensesRepresentation result = expenseService.getExpenses(user.getId(), null, null, null);

        assertNotNull(result);
        assertEquals(0, result.expenses().size());
    }

    @Test
    @TestTransaction
    void testGetExpensesWithNoMatchingFilters() {
        // Create an expense
        AddExpenseCommand command = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.HOUSING,
                EXPENSE_DATE,
                user.getId()
        );

        expenseService.createExpense(command);

        // Search with non-matching filters
        LocalDate futureDate = EXPENSE_DATE.plusDays(10);
        ExpensesRepresentation result = expenseService.getExpenses(
                user.getId(), futureDate, null, null);

        assertEquals(0, result.expenses().size());
    }

    @Test
    @TestTransaction
    void testToAddedExpenseRepresentationList() {
        // Create multiple expenses with different properties
        AddExpenseCommand command1 = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.HOUSING,
                EXPENSE_DATE,
                user.getId()
        );

        AddExpenseCommand command2 = new AddExpenseCommand(
                new Money(new BigDecimal("300.00")),
                ExpenseCategory.OTHER,
                EXPENSE_DATE.minusDays(5),
                user.getId()
        );

        expenseService.createExpense(command1);
        expenseService.createExpense(command2);

        // Get all expenses
        ExpensesRepresentation result = expenseService.getExpenses(
                user.getId(), null, null, null);

        // Verify representation list contains both expenses
        assertEquals(2, result.expenses().size());

        // Verify each expense has the correct representation
        boolean foundSalary = false;
        boolean foundDividends = false;

        for (ExpenseRepresentation expense : result.expenses()) {
            if (expense.category() == ExpenseCategory.HOUSING) {
                assertEquals(EXPENSE_AMOUNT, expense.amount());
                assertEquals(EXPENSE_DATE, expense.date());
                foundSalary = true;
            } else if (expense.category() == ExpenseCategory.OTHER) {
                assertEquals(new BigDecimal("300.00"), expense.amount());
                assertEquals(EXPENSE_DATE.minusDays(5), expense.date());
                foundDividends = true;
            }
        }

        assertTrue(foundSalary);
        assertTrue(foundDividends);
    }
}

