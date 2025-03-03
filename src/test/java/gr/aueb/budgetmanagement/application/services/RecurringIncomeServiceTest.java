package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.commands.AddRecurringIncomeCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.AddedRecurringIncomeRepresentation;
import gr.aueb.budgetmanagement.domain.entities.RecurringIncome;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

@QuarkusTest
class RecurringIncomeServiceTest {
    private static final IncomeCategory VALID_CATEGORY = IncomeCategory.SALARY;
    private static final Money VALID_MONEY = new Money(new BigDecimal("9.99"));

    @Inject
    private UserRepository userRepository;

    @Inject
    private RecurringIncomeService recurringIncomeService;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
    }

    @Test
    @TestTransaction
    void createRecurringIncome_WithValidData() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        AddRecurringIncomeCommand command = new AddRecurringIncomeCommand(
            "Monthly Salary",
            VALID_MONEY,
            VALID_CATEGORY,
            startDate,
            endDate, 
            user.getId()
        );

        // Act
        AddedRecurringIncomeRepresentation result = recurringIncomeService.createRecurringIncome(command);

        // Assert
        assertNotNull(result.id());
        assertEquals("Monthly Salary", result.name());
        assertEquals(VALID_MONEY, result.amount());
        assertEquals(VALID_CATEGORY, result.category());
        assertEquals(startDate, result.startDate());
        assertEquals(endDate, result.endDate());

        // Verify the recurring income was saved in the database
        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
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
    @TestTransaction
    void createRecurringIncome_WithNonExistentUser() {
        // Arrange
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        AddRecurringIncomeCommand command = new AddRecurringIncomeCommand(
            "Freelance Payment",
            VALID_MONEY,
            VALID_CATEGORY,
            startDate,
            endDate, 
            999L
        );

        // Act & Assert
        assertThrows(
            NotFoundException.class,
            () -> recurringIncomeService.createRecurringIncome(command)
        );
    }

    @Test
    @TestTransaction
    void createRecurringIncome_WithInvalidCommand() {
        // Arrange - Create command with empty name
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(12);
        AddRecurringIncomeCommand command = new AddRecurringIncomeCommand(
            "", // Empty name should be invalid
            VALID_MONEY,
            VALID_CATEGORY,
            startDate,
            endDate, 
            user.getId()
        );

        // Act & Assert - Assuming RecurringIncome.create() validates the name
        assertThrows(
            ConstraintViolationException.class,
            () -> recurringIncomeService.createRecurringIncome(command)
        );
    }

    @Test
    @TestTransaction
    void createRecurringIncome_WithInvalidDateRange() {
        // Arrange - Create command with end date before start date
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.minusMonths(1); // End date before start date
        AddRecurringIncomeCommand command = new AddRecurringIncomeCommand(
            "Bonus Payment",
            VALID_MONEY,
            VALID_CATEGORY,
            startDate,
            endDate, 
            user.getId()
        );

        assertThrows(
            Exception.class,
            () -> recurringIncomeService.createRecurringIncome(command)
        );
    }
}
