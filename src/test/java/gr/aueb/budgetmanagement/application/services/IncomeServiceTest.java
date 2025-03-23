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
import gr.aueb.budgetmanagement.application.commands.AddIncomeCommand;
import gr.aueb.budgetmanagement.application.commands.UpdateIncomeCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.IncomeRepresentation;
import gr.aueb.budgetmanagement.application.representations.IncomesRepresentation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class IncomeServiceTest {
    private static final BigDecimal INCOME_AMOUNT = new BigDecimal("150.00");
    private static final LocalDate INCOME_DATE = LocalDate.now();

    @Inject
    private UserRepository userRepository;

    @Inject
    private IncomeService incomeService;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
    }

    @Test
    @TestTransaction
    void testCreateIncome() {
        AddIncomeCommand command = new AddIncomeCommand(
            new Money(INCOME_AMOUNT),
            IncomeCategory.SALARY,
            INCOME_DATE,
            user.getId()
        );

        var result = incomeService.createIncome(command);

        assertNotNull(result.id());
        assertEquals(INCOME_AMOUNT, result.amount());
        assertEquals(INCOME_DATE, result.date());
        assertEquals(IncomeCategory.SALARY, result.category());

        User persistedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(1, persistedUser.getIncomes().size());
    }

    @Test
    @TestTransaction
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
    @TestTransaction
    void testCreateIncomeWithPastDate() {
        LocalDate pastDate = INCOME_DATE.minusDays(5);
        AddIncomeCommand command = new AddIncomeCommand(
            new Money(INCOME_AMOUNT),
            IncomeCategory.SALARY,
            pastDate,
            user.getId()
        );

        var result = incomeService.createIncome(command);

        assertNotNull(result.id());
        assertEquals(INCOME_AMOUNT, result.amount());
        assertEquals(pastDate, result.date());
        assertEquals(IncomeCategory.SALARY, result.category());
    }

    @Test
    @TestTransaction
    void testGetIncomesWithDateFilters() {
        // Create incomes with different dates
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);

        AddIncomeCommand command1 = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.SALARY,
                today,
                user.getId()
        );

        AddIncomeCommand command2 = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.DIVIDENDS,
                yesterday,
                user.getId()
        );

        AddIncomeCommand command3 = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.OTHER,
                lastWeek,
                user.getId()
        );

        incomeService.createIncome(command1);
        incomeService.createIncome(command2);
        incomeService.createIncome(command3);

        // Test fromDate filter
        IncomesRepresentation resultFromDate = incomeService.getIncomes(
                user.getId(), yesterday, null, null);
        assertEquals(2, resultFromDate.incomes().size());

        // Test toDate filter
        IncomesRepresentation resultToDate = incomeService.getIncomes(
                user.getId(), null, yesterday, null);
        assertEquals(2, resultToDate.incomes().size());

        // Test both date filters
        IncomesRepresentation resultBothDates = incomeService.getIncomes(
                user.getId(), yesterday, today, null);
        assertEquals(2, resultBothDates.incomes().size());
    }

    @Test
    @TestTransaction
    void testGetIncomesWithCategoryFilter() {
        // Create incomes with different categories
        AddIncomeCommand command1 = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.SALARY,
                INCOME_DATE,
                user.getId()
        );

        AddIncomeCommand command2 = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.DIVIDENDS,
                INCOME_DATE,
                user.getId()
        );

        AddIncomeCommand command3 = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.SALARY,
                INCOME_DATE,
                user.getId()
        );

        incomeService.createIncome(command1);
        incomeService.createIncome(command2);
        incomeService.createIncome(command3);

        // Test category filter
        IncomesRepresentation result = incomeService.getIncomes(
                user.getId(), null, null, IncomeCategory.SALARY);

        assertEquals(2, result.incomes().size());
        for (IncomeRepresentation income : result.incomes()) {
            assertEquals(IncomeCategory.SALARY, income.category());
        }
    }

    @Test
    @TestTransaction
    void testGetIncomesWithAllFilters() {
        // Create incomes with different dates and categories
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate lastWeek = today.minusDays(7);

        AddIncomeCommand command1 = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.SALARY,
                today,
                user.getId()
        );

        AddIncomeCommand command2 = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.DIVIDENDS,
                yesterday,
                user.getId()
        );

        AddIncomeCommand command3 = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.SALARY,
                lastWeek,
                user.getId()
        );

        incomeService.createIncome(command1);
        incomeService.createIncome(command2);
        incomeService.createIncome(command3);

        // Test with all filters
        IncomesRepresentation result = incomeService.getIncomes(
                user.getId(), yesterday, today, IncomeCategory.SALARY);

        assertEquals(1, result.incomes().size());
        assertEquals(IncomeCategory.SALARY, result.incomes().get(0).category());
    }

    @Test
    @TestTransaction
    void testUpdateIncome() {
        // First create an income
        AddIncomeCommand createCommand = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.SALARY,
                INCOME_DATE,
                user.getId()
        );

        IncomeRepresentation createdIncome = incomeService.createIncome(createCommand);

        // Now update it
        BigDecimal updatedAmount = new BigDecimal("200.00");
        LocalDate updatedDate = INCOME_DATE.plusDays(1);
        IncomeCategory updatedCategory = IncomeCategory.DIVIDENDS;

        UpdateIncomeCommand updateCommand = new UpdateIncomeCommand(
                createdIncome.id(),
                user.getId(),
                new Money(updatedAmount),
                updatedDate,
                updatedCategory
        );

        IncomeRepresentation updatedIncome = incomeService.updateIncome(updateCommand);

        assertNotNull(updatedIncome);
        assertEquals(createdIncome.id(), updatedIncome.id());
        assertEquals(updatedAmount, updatedIncome.amount());
        assertEquals(updatedDate, updatedIncome.date());
        assertEquals(updatedCategory, updatedIncome.category());

        User persistedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(1, persistedUser.getIncomes().size());


    }

    @Test
    @TestTransaction
    void testUpdateIncomeUserNotFound() {
        // First create an income with the valid user
        AddIncomeCommand createCommand = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.SALARY,
                INCOME_DATE,
                user.getId()
        );

        IncomeRepresentation createdIncome = incomeService.createIncome(createCommand);

        // Now try to update with a non-existent user ID (999L)
        UpdateIncomeCommand updateCommand = new UpdateIncomeCommand(
                createdIncome.id(),
                999L, // Non-existent user ID
                new Money(new BigDecimal("200.00")),
                INCOME_DATE,
                IncomeCategory.SALARY
        );

        assertThrows(
                NotFoundException.class,
                () -> incomeService.updateIncome(updateCommand)
        );
    }

    @Test
    @TestTransaction
    void testUpdateIncomeNotFound() {
        UpdateIncomeCommand updateCommand = new UpdateIncomeCommand(
                999L,
                user.getId(),
                new Money(new BigDecimal("200.00")),
                INCOME_DATE,
                IncomeCategory.SALARY
        );

        assertThrows(
                NotFoundException.class,
                () -> incomeService.updateIncome(updateCommand)
        );
    }

    @Test
    @TestTransaction
    void testDeleteIncome() {
        // First create an income
        AddIncomeCommand createCommand = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.SALARY,
                INCOME_DATE,
                user.getId()
        );

        IncomeRepresentation createdIncome = incomeService.createIncome(createCommand);

        // Verify income was created
        User userBeforeDelete = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(1, userBeforeDelete.getIncomes().size());

        // Now delete it
        incomeService.deleteIncome(createdIncome.id(), user.getId());

        // Verify income was deleted
        User userAfterDelete = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(0, userAfterDelete.getIncomes().size());
    }

    @Test
    @TestTransaction
    void testDeleteIncomeUserNotFound() {
        assertThrows(
                NotFoundException.class,
                () -> incomeService.deleteIncome(1L, 999L)
        );
    }

    @Test
    @TestTransaction
    void testGetIncomesUserNotFound() {
        assertThrows(
                NotFoundException.class,
                () -> incomeService.getIncomes(999L, null, null, null)
        );
    }

    @Test
    @TestTransaction
    void testGetIncomesWithNoIncomes() {
        // User exists but has no incomes
        IncomesRepresentation result = incomeService.getIncomes(user.getId(), null, null, null);

        assertNotNull(result);
        assertEquals(0, result.incomes().size());
    }

    @Test
    @TestTransaction
    void testGetIncomesWithNoMatchingFilters() {
        // Create an income
        AddIncomeCommand command = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.SALARY,
                INCOME_DATE,
                user.getId()
        );

        incomeService.createIncome(command);

        // Search with non-matching filters
        LocalDate futureDate = INCOME_DATE.plusDays(10);
        IncomesRepresentation result = incomeService.getIncomes(
                user.getId(), futureDate, null, null);

        assertEquals(0, result.incomes().size());
    }

    @Test
    @TestTransaction
    void testToAddedIncomeRepresentationList() {
        // Create multiple incomes with different properties
        AddIncomeCommand command1 = new AddIncomeCommand(
                new Money(INCOME_AMOUNT),
                IncomeCategory.SALARY,
                INCOME_DATE,
                user.getId()
        );

        AddIncomeCommand command2 = new AddIncomeCommand(
                new Money(new BigDecimal("300.00")),
                IncomeCategory.DIVIDENDS,
                INCOME_DATE.minusDays(5),
                user.getId()
        );

        incomeService.createIncome(command1);
        incomeService.createIncome(command2);

        // Get all incomes
        IncomesRepresentation result = incomeService.getIncomes(
                user.getId(), null, null, null);

        // Verify representation list contains both incomes
        assertEquals(2, result.incomes().size());

        // Verify each income has the correct representation
        boolean foundSalary = false;
        boolean foundDividends = false;

        for (IncomeRepresentation income : result.incomes()) {
            if (income.category() == IncomeCategory.SALARY) {
                assertEquals(INCOME_AMOUNT, income.amount());
                assertEquals(INCOME_DATE, income.date());
                foundSalary = true;
            } else if (income.category() == IncomeCategory.DIVIDENDS) {
                assertEquals(new BigDecimal("300.00"), income.amount());
                assertEquals(INCOME_DATE.minusDays(5), income.date());
                foundDividends = true;
            }
        }

        assertTrue(foundSalary);
        assertTrue(foundDividends);
    }

}
