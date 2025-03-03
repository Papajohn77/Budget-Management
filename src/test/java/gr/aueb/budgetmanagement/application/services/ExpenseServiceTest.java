package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.commands.AddExpenseCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class ExpenseServiceTest {
    private static final BigDecimal EXPENSE_AMOUNT = new BigDecimal("150.00");
    private static final LocalDate EXPENSE_DATE = LocalDate.now();

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
        assertEquals(EXPENSE_AMOUNT, result.amount().getValue());
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
        assertEquals(EXPENSE_AMOUNT, result.amount().getValue());
        assertEquals(pastDate, result.date());
        assertEquals(ExpenseCategory.HEALTH, result.category());
    }
}
