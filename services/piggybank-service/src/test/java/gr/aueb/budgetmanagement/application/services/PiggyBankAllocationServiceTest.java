package gr.aueb.budgetmanagement.application.services;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.commands.AllocateToPiggyBankCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.PiggyBankRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.PiggyBankAllocationRepresentation;
import gr.aueb.budgetmanagement.domain.entities.GroupPiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PersonalPiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PiggyBank;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.exceptions.ForbiddenOperationDomainException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class PiggyBankAllocationServiceTest {
    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 15);
    private static final LocalDate TODAY = FIXED_DATE;
    private static final BigDecimal AMOUNT = new BigDecimal("100.00");

    @Inject
    private UserRepository userRepository;

    @Inject
    private PiggyBankRepository piggyBankRepository;

    @Inject
    private PiggyBankAllocationService allocationService;

    private User user;
    private User otherUser;
    private PersonalPiggyBank personalPiggyBank;
    private GroupPiggyBank groupPiggyBank;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
        otherUser = userRepository.findById(Fixture.Users.TESTUSER2_ID).orElseThrow();
        personalPiggyBank = (PersonalPiggyBank) piggyBankRepository.findById(Fixture.PiggyBanks.PERSONAL_PIGGY_BANK_ID).orElseThrow();
        groupPiggyBank = (GroupPiggyBank) piggyBankRepository.findById(Fixture.PiggyBanks.GROUP_PIGGY_BANK_ID).orElseThrow();
    }

    @Test
    @TestTransaction
    void testSuccessfulPersonalPiggyBankAllocation() {
        PiggyBank initialPiggyBank = piggyBankRepository.findById(personalPiggyBank.getId()).orElseThrow();
        int initialCount = initialPiggyBank.getAllocations().size();

        // Arrange
        AllocateToPiggyBankCommand command = new AllocateToPiggyBankCommand(
            TODAY,
            new Money(AMOUNT),
            personalPiggyBank.getId(),
            user.getId()
        );

        // Act
        PiggyBankAllocationRepresentation result = allocationService.allocateToPiggyBank(command);

        // Assert
        assertNotNull(result.id());
        assertEquals(TODAY, result.date());
        assertEquals(AMOUNT, result.amount());
        assertEquals(personalPiggyBank.getId(), result.piggyBankId());

        // Verify persistence
        PiggyBank persistedPiggyBank = piggyBankRepository.findById(personalPiggyBank.getId()).orElseThrow();
        assertEquals(initialCount + 1, persistedPiggyBank.getAllocations().size());
    }

    @Test
    @TestTransaction
    void testSuccessfulGroupPiggyBankAllocation() {
        // Arrange
        AllocateToPiggyBankCommand command = new AllocateToPiggyBankCommand(
            TODAY,
            new Money(AMOUNT),
            groupPiggyBank.getId(),
            user.getId()
        );

        // Act
        PiggyBankAllocationRepresentation result = allocationService.allocateToPiggyBank(command);

        // Assert
        assertNotNull(result.id());
        assertEquals(TODAY, result.date());
        assertEquals(AMOUNT, result.amount());
        assertEquals(groupPiggyBank.getId(), result.piggyBankId());

        // Verify persistence
        PiggyBank persistedPiggyBank = piggyBankRepository.findById(groupPiggyBank.getId()).orElseThrow();
        assertEquals(1, persistedPiggyBank.getAllocations().size());
    }

    @Test
    @TestTransaction
    void testAllocationWithNonexistentUser() {
        // Arrange
        AllocateToPiggyBankCommand command = new AllocateToPiggyBankCommand(
            TODAY,
            new Money(AMOUNT),
            personalPiggyBank.getId(),
            999L
        );

        // Act & Assert
        assertThrows(
            NotFoundException.class,
            () -> allocationService.allocateToPiggyBank(command)
        );
    }

    @Test
    @TestTransaction
    void testAllocationWithNonexistentPiggyBank() {
        // Arrange
        AllocateToPiggyBankCommand command = new AllocateToPiggyBankCommand(
            TODAY,
            new Money(AMOUNT),
            999L,
            user.getId()
        );

        // Act & Assert
        assertThrows(
            NotFoundException.class,
            () -> allocationService.allocateToPiggyBank(command)
        );
    }

    @Test
    @TestTransaction
    void testAllocationToUnauthorizedPersonalPiggyBank() {
        // Arrange
        AllocateToPiggyBankCommand command = new AllocateToPiggyBankCommand(
            TODAY,
            new Money(AMOUNT),
            personalPiggyBank.getId(),
            otherUser.getId()
        );

        // Act & Assert
        assertThrows(
            ForbiddenOperationDomainException.class,
            () -> allocationService.allocateToPiggyBank(command)
        );
    }

    @Test
    @TestTransaction
    void testAllocationToUnauthorizedGroupPiggyBank() {
        // Arrange
        AllocateToPiggyBankCommand command = new AllocateToPiggyBankCommand(
            TODAY,
            new Money(AMOUNT),
            groupPiggyBank.getId(),
            otherUser.getId()
        );

        // Act & Assert
        assertThrows(
            ForbiddenOperationDomainException.class,
            () -> allocationService.allocateToPiggyBank(command)
        );
    }
}
