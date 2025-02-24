package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.application.commands.AddExpenseCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class ExpenseServiceTest {
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test123!@#";
    private static final BigDecimal EXPENSE_AMOUNT = new BigDecimal("150.00");
    private static final LocalDate EXPENSE_DATE = LocalDate.now();

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private UserRepository userRepository;
    private ExpenseService expenseService;
    private User user;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        userRepository = new JpaUserRepository(entityManager);
        expenseService = new ExpenseService(userRepository);

        createTestUser();
    }

    @AfterEach
    void tearDown() {
        if (transaction.isActive()) {
            transaction.rollback();
        }
        entityManager.close();
    }

    private void createTestUser() {
        user = User.create(
                TEST_USERNAME,
                TEST_EMAIL,
                TEST_PASSWORD,
                new BCryptPasswordEncoder()
        );
        entityManager.persist(user);
    }

    @Test
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
    void testCreateExpenseWithEntertainmentCategory() {
        AddExpenseCommand command = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.ENTERTAINMENT,
                EXPENSE_DATE,
                user.getId()
        );

        var result = expenseService.createExpense(command);

        assertNotNull(result.id());
        assertEquals(EXPENSE_AMOUNT, result.amount().getValue());
        assertEquals(EXPENSE_DATE, result.date());
        assertEquals(ExpenseCategory.ENTERTAINMENT, result.category());
    }

    @Test
    void testCreateExpenseWithTransportationCategory() {
        AddExpenseCommand command = new AddExpenseCommand(
                new Money(EXPENSE_AMOUNT),
                ExpenseCategory.TRANSPORTATION,
                EXPENSE_DATE,
                user.getId()
        );

        var result = expenseService.createExpense(command);

        assertNotNull(result.id());
        assertEquals(EXPENSE_AMOUNT, result.amount().getValue());
        assertEquals(EXPENSE_DATE, result.date());
        assertEquals(ExpenseCategory.TRANSPORTATION, result.category());
    }

    @Test
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
    void testCreateExpenseWithDifferentAmount() {
        BigDecimal differentAmount = new BigDecimal("250.50");
        AddExpenseCommand command = new AddExpenseCommand(
                new Money(differentAmount),
                ExpenseCategory.FOOD,
                EXPENSE_DATE,
                user.getId()
        );

        var result = expenseService.createExpense(command);

        assertNotNull(result.id());
        assertEquals(differentAmount, result.amount().getValue());
        assertEquals(EXPENSE_DATE, result.date());
        assertEquals(ExpenseCategory.FOOD, result.category());
    }

    @Test
    void testCreateExpenseWithPastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(5);
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