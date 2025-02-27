package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.time.LocalDate;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.ports.PasswordHasher;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.application.commands.CreateRecurringIncomeCommand;
import gr.aueb.budgetmanagement.application.dto.CreatedRecurringIncomeDTO;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.domain.entities.RecurringIncome;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaUserRepository;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class RecurringIncomeServiceTest {
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test123!@#";
    private static final IncomeCategory VALID_CATEGORY = IncomeCategory.SALARY;
    private static final Money VALID_MONEY = new Money(new BigDecimal("9.99"));

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private JpaUserRepository userRepository;
    private RecurringIncomeService recurringIncomeService;
    private PasswordHasher passwordHasher;
    private User user;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        userRepository = new JpaUserRepository(entityManager);
        recurringIncomeService = new RecurringIncomeService(userRepository);
        passwordHasher = new BCryptPasswordEncoder();

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
                new BCryptPasswordEncoder());

        entityManager.persist(user);
    }

    @Test
    void createRecurringIncome_WithValidData() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        CreateRecurringIncomeCommand command = new CreateRecurringIncomeCommand(
                user.getId(),
                "Monthly Salary",
                VALID_MONEY,
                VALID_CATEGORY,
                startDate,
                endDate
        );

        // Act
        CreatedRecurringIncomeDTO result = recurringIncomeService.createRecurringIncome(command);

        // Assert
        assertNotNull(result.id());
        assertEquals("Monthly Salary", result.name());
        assertEquals(VALID_MONEY, result.amount());
        assertEquals(VALID_CATEGORY, result.category());
        assertEquals(startDate, result.startDate());
        assertEquals(endDate, result.endDate());

        // Verify the recurring income was saved in the database
        User updatedUser = entityManager.find(User.class, user.getId());
        assertNotNull(updatedUser);

        RecurringIncome savedIncome = null;
        for (RecurringIncome income : updatedUser.getRecurringIncomes()) {
            if (income.getId().equals(result.id())) {
                savedIncome = income;
                break;
            }
        }

        assertNotNull(savedIncome);
        assertEquals("Monthly Salary", savedIncome.getName());
        assertEquals(VALID_MONEY, savedIncome.getAmount());
        assertEquals(VALID_CATEGORY, savedIncome.getCategory());
        assertEquals(startDate, savedIncome.getStartDate());
        assertEquals(endDate, savedIncome.getEndDate());
        assertFalse(savedIncome.isStopped());
    }

    @Test
    void createRecurringIncome_WithNonExistentUser() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        CreateRecurringIncomeCommand command = new CreateRecurringIncomeCommand(
                999L,
                "Freelance Payment",
                VALID_MONEY,
                VALID_CATEGORY,
                startDate,
                endDate
        );

        // Act & Assert
        assertThrows(
                NotFoundException.class,
                () -> recurringIncomeService.createRecurringIncome(command)
        );
    }

    @Test
    void createRecurringIncome_WithInvalidCommand() {
        // Arrange - Create command with empty name
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        CreateRecurringIncomeCommand command = new CreateRecurringIncomeCommand(
                user.getId(),
                "", // Empty name should be invalid
                VALID_MONEY,
                VALID_CATEGORY,
                startDate,
                endDate
        );

        // Act & Assert - Assuming RecurringIncome.create() validates the name
        assertThrows(
                InvalidDomainArgumentException.class,
                () -> recurringIncomeService.createRecurringIncome(command)
        );
    }

    @Test
    void createRecurringIncome_WithInvalidDateRange() {
        // Arrange - Create command with end date before start date
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.minusMonths(1); // End date before start date
        CreateRecurringIncomeCommand command = new CreateRecurringIncomeCommand(
                user.getId(),
                "Bonus Payment",
                VALID_MONEY,
                VALID_CATEGORY,
                startDate,
                endDate
        );

        assertThrows(
                Exception.class,
                () -> recurringIncomeService.createRecurringIncome(command)
        );
    }
}
