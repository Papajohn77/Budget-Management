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

import gr.aueb.budgetmanagement.application.commands.AddIncomeCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class IncomeServiceTest {
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test123!@#";
    private static final BigDecimal INCOME_AMOUNT = new BigDecimal("150.00");
    private static final LocalDate INCOME_DATE = LocalDate.now();

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private UserRepository userRepository;
    private IncomeService incomeService;
    private User user;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        userRepository = new JpaUserRepository(entityManager);
        incomeService = new IncomeService(userRepository);

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
    void testCreateIncome() {
        AddIncomeCommand command = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.SALARY,
                INCOME_DATE,
                user.getId()
        );

        var result = incomeService.createIncome(command);

        assertNotNull(result.id());
        assertEquals(INCOME_AMOUNT, result.amount().getValue());
        assertEquals(INCOME_DATE, result.date());
        assertEquals(IncomeCategory.SALARY, result.category());

        User persistedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(1, persistedUser.getIncomes().size());
    }

    @Test
    void testCreateIncomeWithEntertainmentCategory() {
        AddIncomeCommand command = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.DIVIDENDS,
                INCOME_DATE,
                user.getId()
        );

        var result = incomeService.createIncome(command);

        assertNotNull(result.id());
        assertEquals(INCOME_AMOUNT, result.amount().getValue());
        assertEquals(INCOME_DATE, result.date());
        assertEquals(IncomeCategory.DIVIDENDS, result.category());
    }

    @Test
    void testCreateIncomeWithTransportationCategory() {
        AddIncomeCommand command = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.SALARY,
                INCOME_DATE,
                user.getId()
        );

        var result = incomeService.createIncome(command);

        assertNotNull(result.id());
        assertEquals(INCOME_AMOUNT, result.amount().getValue());
        assertEquals(INCOME_DATE, result.date());
        assertEquals(IncomeCategory.SALARY, result.category());
    }

    @Test
    void testCreateIncomeUserNotFound() {
        AddIncomeCommand command = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.DIVIDENDS,
                INCOME_DATE,
                999L
        );

        assertThrows(
                NotFoundException.class,
                () -> incomeService.createIncome(command)
        );
    }

    @Test
    void testCreateIncomeWithDifferentAmount() {
        BigDecimal differentAmount = new BigDecimal("250.50");
        AddIncomeCommand command = new AddIncomeCommand(
                new Money(differentAmount),
                IncomeCategory.SALARY,
                INCOME_DATE,
                user.getId()
        );

        var result = incomeService.createIncome(command);

        assertNotNull(result.id());
        assertEquals(differentAmount, result.amount().getValue());
        assertEquals(INCOME_DATE, result.date());
        assertEquals(IncomeCategory.SALARY, result.category());
    }

    @Test
    void testCreateIncomeWithPastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(5);
        AddIncomeCommand command = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.SALARY,
                pastDate,
                user.getId()
        );

        var result = incomeService.createIncome(command);

        assertNotNull(result.id());
        assertEquals(INCOME_AMOUNT, result.amount().getValue());
        assertEquals(pastDate, result.date());
        assertEquals(IncomeCategory.SALARY, result.category());
    }
}