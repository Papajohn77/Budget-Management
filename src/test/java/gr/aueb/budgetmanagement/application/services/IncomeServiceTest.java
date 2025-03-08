package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.commands.AddIncomeCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
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
        assertEquals(INCOME_AMOUNT, result.amount().getValue());
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
        assertEquals(INCOME_AMOUNT, result.amount().getValue());
        assertEquals(pastDate, result.date());
        assertEquals(IncomeCategory.SALARY, result.category());
    }
}
