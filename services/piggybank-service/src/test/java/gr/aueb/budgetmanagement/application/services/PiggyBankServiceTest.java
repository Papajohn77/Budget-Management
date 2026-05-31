package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.commands.AllocateToPiggyBankCommand;
import gr.aueb.budgetmanagement.application.commands.CreateGroupPiggyBankCommand;
import gr.aueb.budgetmanagement.application.commands.CreatePersonalPiggyBankCommand;
import gr.aueb.budgetmanagement.application.commands.DissolvePiggyBankCommand;
import gr.aueb.budgetmanagement.application.exceptions.ForbiddenException;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.GroupRepository;
import gr.aueb.budgetmanagement.application.repositories.PiggyBankRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.PiggyBanksRepresentation;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.PiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PiggyBankAllocation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.exceptions.ForbiddenOperationDomainException;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
class PiggyBankServiceTest {
    private static final LocalDate FIXED_DATE = LocalDate.of(2024, 1, 15);
    private static final BigDecimal TARGET_AMOUNT = new BigDecimal("1000.00");
    private static final String PIGGY_BANK_NAME = "Test Piggy Bank";

    @Inject
    EntityManager entityManager;

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private PiggyBankRepository piggyBankRepository;

    @Inject
    private PiggyBankService piggyBankService;

    @Inject
    private PiggyBankAllocationService piggyBankAllocationService;

    private User admin;

    private User nonAdmin;

    private Group group;

    @BeforeEach
    void setUp() {
        admin = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
        nonAdmin = userRepository.findById(Fixture.Users.TESTUSER2_ID).orElseThrow();
        group = groupRepository.findById(Fixture.Groups.TESTGROUP_ID).orElseThrow();
    }

    @Test
    @TestTransaction
    void testCreatePersonalPiggyBank() {
        int adminNumOfPiggyBanks = 1;
        assertEquals(adminNumOfPiggyBanks, admin.getPiggyBanks().size());

        CreatePersonalPiggyBankCommand command = new CreatePersonalPiggyBankCommand(
            PIGGY_BANK_NAME,
            new Money(TARGET_AMOUNT),
            ExpenseCategory.OTHER,
            admin.getId()
        );

        var result = piggyBankService.createPersonalPiggyBank(command);

        assertNotNull(result.id());
        assertEquals(PIGGY_BANK_NAME, result.name());
        assertEquals(TARGET_AMOUNT, result.targetAmount());
        assertEquals(BigDecimal.ZERO, result.currentAmount());
        assertEquals(ExpenseCategory.OTHER, result.category());

        User persistedUser = userRepository.findById(admin.getId()).orElseThrow();
        assertEquals(adminNumOfPiggyBanks + 1, persistedUser.getPiggyBanks().size());
    }

    @Test
    @TestTransaction
    void testCreatePersonalPiggyBankUserNotFound() {
        CreatePersonalPiggyBankCommand command = new CreatePersonalPiggyBankCommand(
            PIGGY_BANK_NAME,
            new Money(TARGET_AMOUNT),
            ExpenseCategory.OTHER,
            999L
        );

        assertThrows(
            NotFoundException.class,
            () -> piggyBankService.createPersonalPiggyBank(command)
        );
    }

    @Test
    @TestTransaction
    void testCreateGroupPiggyBank() {
        int groupNumOfPiggyBanks = 1;
        assertEquals(groupNumOfPiggyBanks, admin.getPiggyBanks().size());

        CreateGroupPiggyBankCommand command = new CreateGroupPiggyBankCommand(
            PIGGY_BANK_NAME,
            new Money(TARGET_AMOUNT),
            ExpenseCategory.OTHER,
            group.getId(),
            admin.getId()
        );

        var result = piggyBankService.createGroupPiggyBank(command);

        assertNotNull(result.id());
        assertEquals(PIGGY_BANK_NAME, result.name());
        assertEquals(TARGET_AMOUNT, result.targetAmount());
        assertEquals(BigDecimal.ZERO, result.currentAmount());
        assertEquals(ExpenseCategory.OTHER, result.category());
        assertEquals(group.getId(), result.groupId());

        Group persistedGroup = groupRepository.findById(group.getId()).orElseThrow();
        assertEquals(groupNumOfPiggyBanks + 1, persistedGroup.getPiggyBanks().size());
    }

    @Test
    @TestTransaction
    void testCreateGroupPiggyBankGroupNotFound() {
        CreateGroupPiggyBankCommand command = new CreateGroupPiggyBankCommand(
            PIGGY_BANK_NAME,
            new Money(TARGET_AMOUNT),
            ExpenseCategory.OTHER,
            999L,
            admin.getId()
        );

        assertThrows(
            NotFoundException.class,
            () -> piggyBankService.createGroupPiggyBank(command)
        );
    }

    @Test
    @TestTransaction
    void testCreateGroupPiggyBankAdminNotFound() {
        CreateGroupPiggyBankCommand command = new CreateGroupPiggyBankCommand(
            PIGGY_BANK_NAME,
            new Money(TARGET_AMOUNT),
            ExpenseCategory.OTHER,
            group.getId(),
            999L
        );

        assertThrows(
            NotFoundException.class,
            () -> piggyBankService.createGroupPiggyBank(command)
        );
    }

    @Test
    @TestTransaction
    void testCreateGroupPiggyBankNotAdmin() {
        CreateGroupPiggyBankCommand command = new CreateGroupPiggyBankCommand(
            PIGGY_BANK_NAME,
            new Money(TARGET_AMOUNT),
            ExpenseCategory.OTHER,
            group.getId(),
            nonAdmin.getId()
        );

        assertThrows(
            ForbiddenOperationDomainException.class,
            () -> piggyBankService.createGroupPiggyBank(command)
        );
    }

    @Test
    @TestTransaction
    void testDissolvePersonalPiggyBank() {
        // Arrange
        CreatePersonalPiggyBankCommand createCommand = new CreatePersonalPiggyBankCommand(
            PIGGY_BANK_NAME,
            new Money(TARGET_AMOUNT),
            ExpenseCategory.OTHER,
            admin.getId()
        );
        var created = piggyBankService.createPersonalPiggyBank(createCommand);

        PiggyBank personalPiggyBank = piggyBankRepository
            .findById(created.id())
            .orElseThrow();

        PiggyBankAllocation allocation = personalPiggyBank.allocate(
            new Money(BigDecimal.valueOf(500)),
            FIXED_DATE,
            admin
        );
        entityManager.persist(allocation);

        // Act
        DissolvePiggyBankCommand dissolveCommand = new DissolvePiggyBankCommand(
            created.id(),
            admin.getId()
        );
        piggyBankService.dissolvePiggyBank(dissolveCommand);

        // Assert
        assertFalse(piggyBankRepository.findById(created.id()).isPresent());
        assertNull(entityManager.find(PiggyBankAllocation.class, allocation.getId()));
    }

    @Test
    @TestTransaction
    void testDissolveGroupPiggyBank() {
        // Arrange
        CreateGroupPiggyBankCommand createCommand = new CreateGroupPiggyBankCommand(
            PIGGY_BANK_NAME,
            new Money(TARGET_AMOUNT),
            ExpenseCategory.OTHER,
            group.getId(),
            admin.getId()
        );
        var created = piggyBankService.createGroupPiggyBank(createCommand);

        PiggyBank groupPiggyBank = piggyBankRepository
            .findById(created.id())
            .orElseThrow();

        PiggyBankAllocation allocation = groupPiggyBank.allocate(
            new Money(BigDecimal.valueOf(500)),
            FIXED_DATE,
            admin
        );
        entityManager.persist(allocation);

        // Act
        DissolvePiggyBankCommand dissolveCommand = new DissolvePiggyBankCommand(
            created.id(),
            admin.getId()
        );
        piggyBankService.dissolvePiggyBank(dissolveCommand);

        // Assert
        assertFalse(piggyBankRepository.findById(created.id()).isPresent());
        assertNull(entityManager.find(PiggyBankAllocation.class, allocation.getId()));
    }

    @Test
    @TestTransaction
    void testDissolvePiggyBankPiggyBankNotFound() {
        DissolvePiggyBankCommand command = new DissolvePiggyBankCommand(999L, admin.getId());
        assertThrows(
            NotFoundException.class,
            () -> piggyBankService.dissolvePiggyBank(command)
        );
    }

    @Test
    @TestTransaction
    void testDissolvePiggyBankPiggyBankOwnerNotFound() {
        DissolvePiggyBankCommand command = new DissolvePiggyBankCommand(Fixture.PiggyBanks.PERSONAL_PIGGY_BANK_ID, 999L);
        assertThrows(
            NotFoundException.class,
            () -> piggyBankService.dissolvePiggyBank(command)
        );
    }

    @Test
    @TestTransaction
    void testDissolvePersonalPiggyBankNotOwner() {
        // Arrange
        CreatePersonalPiggyBankCommand createCommand = new CreatePersonalPiggyBankCommand(
            PIGGY_BANK_NAME,
            new Money(TARGET_AMOUNT),
            ExpenseCategory.OTHER,
            admin.getId()
        );
        var created = piggyBankService.createPersonalPiggyBank(createCommand);

        // Act & Assert
        DissolvePiggyBankCommand dissolveCommand = new DissolvePiggyBankCommand(created.id(), nonAdmin.getId());
        assertThrows(
            ForbiddenException.class,
            () -> piggyBankService.dissolvePiggyBank(dissolveCommand)
        );
    }

    @Test
    @TestTransaction
    void testDissolveGroupPiggyBankNotAdmin() {
        // Arrange
        CreateGroupPiggyBankCommand createCommand = new CreateGroupPiggyBankCommand(
            PIGGY_BANK_NAME,
            new Money(TARGET_AMOUNT),
            ExpenseCategory.OTHER,
            group.getId(),
            admin.getId()
        );
        var created = piggyBankService.createGroupPiggyBank(createCommand);

        group.addMember(nonAdmin);

        // Act & Assert
        DissolvePiggyBankCommand dissolveCommand = new DissolvePiggyBankCommand(created.id(), nonAdmin.getId());
        assertThrows(
            ForbiddenException.class,
            () -> piggyBankService.dissolvePiggyBank(dissolveCommand)
        );
    }

    @Test
    @TestTransaction
    void testGetAllPiggyBanks() {
        // Act
        PiggyBanksRepresentation result = piggyBankService.getPiggyBanks(admin.getId(), null);

        // Verify personal piggy bank from import.sql
        assertNotNull(result.personalPiggyBanks());
        assertEquals(1, result.personalPiggyBanks().size());

        var personalPiggyBank = result.personalPiggyBanks().get(0);
        assertEquals(Fixture.PiggyBanks.PERSONAL_PIGGY_BANK_ID, personalPiggyBank.id());
        assertEquals("testpersonalpiggy", personalPiggyBank.name());
        assertEquals(new BigDecimal("1000.00"), personalPiggyBank.targetAmount());
        assertEquals(new BigDecimal("250.00"), personalPiggyBank.currentAmount());
        assertEquals(ExpenseCategory.ENTERTAINMENT, personalPiggyBank.category());

        // Verify group piggy bank from import.sql
        assertNotNull(result.groupPiggyBanks());
        assertEquals(1, result.groupPiggyBanks().size());

        var group = result.groupPiggyBanks().get(0);
        assertEquals("testgroup", group.name());
        assertEquals(Fixture.Groups.TESTGROUP_ID, group.groupId());
        assertEquals(1, group.piggyBanks().size());

        var groupPiggyBank = group.piggyBanks().get(0);
        assertEquals(Fixture.PiggyBanks.GROUP_PIGGY_BANK_ID, groupPiggyBank.id());
        assertEquals("testgrouppiggy", groupPiggyBank.name());
        assertEquals(new BigDecimal("2000.00"), groupPiggyBank.targetAmount());
        assertEquals(BigDecimal.ZERO, groupPiggyBank.currentAmount());
        assertEquals(ExpenseCategory.ENTERTAINMENT, groupPiggyBank.category());
        assertEquals(Fixture.Groups.TESTGROUP_ID, groupPiggyBank.groupId());
    }

    @Test
    @TestTransaction
    void testGetOnlyPersonalPiggyBanks() {
        // Act
        PiggyBanksRepresentation result = piggyBankService.getPiggyBanks(admin.getId(), "personal");

        // Verify personal piggy banks
        assertNotNull(result.personalPiggyBanks());
        assertEquals(1, result.personalPiggyBanks().size());
        assertEquals(Fixture.PiggyBanks.PERSONAL_PIGGY_BANK_ID, result.personalPiggyBanks().get(0).id());

        // Verify no group piggy banks
        assertNotNull(result.groupPiggyBanks());
        assertTrue(result.groupPiggyBanks().isEmpty());
    }

    @Test
    @TestTransaction
    void testGetOnlyGroupPiggyBanks() {
        // Act
        PiggyBanksRepresentation result = piggyBankService.getPiggyBanks(admin.getId(), "group");

        // Verify no personal piggy banks
        assertNotNull(result.personalPiggyBanks());
        assertTrue(result.personalPiggyBanks().isEmpty());

        // Verify group piggy banks
        assertNotNull(result.groupPiggyBanks());
        assertEquals(1, result.groupPiggyBanks().size());

        var group = result.groupPiggyBanks().get(0);
        assertEquals(Fixture.Groups.TESTGROUP_ID, group.groupId());
        assertEquals(1, group.piggyBanks().size());
    }

    @Test
    @TestTransaction
    void testGetPiggyBanksForUserWithNoPersonalPiggyBanks() {
        // Act
        PiggyBanksRepresentation result = piggyBankService.getPiggyBanks(nonAdmin.getId(), null);

        // Verify no personal piggy banks
        assertNotNull(result.personalPiggyBanks());
        assertTrue(result.personalPiggyBanks().isEmpty());

        // Verify no group piggy banks
        assertNotNull(result.groupPiggyBanks());
        assertTrue(result.groupPiggyBanks().isEmpty());
    }

    @Test
    @TestTransaction
    void testGetPiggyBanksForGroupMember() {
        // Arrange
        User member = userRepository.findById(Fixture.Users.TESTUSER3_ID).orElseThrow();
        
        // Act
        PiggyBanksRepresentation result = piggyBankService.getPiggyBanks(member.getId(), null);

        // Verify no personal piggy banks
        assertNotNull(result.personalPiggyBanks());
        assertTrue(result.personalPiggyBanks().isEmpty());
        
        // Verify member can see group piggy banks
        assertNotNull(result.groupPiggyBanks());
        assertEquals(1, result.groupPiggyBanks().size());

        var group = result.groupPiggyBanks().get(0);
        assertEquals(1, group.piggyBanks().size());
        assertEquals(Fixture.PiggyBanks.GROUP_PIGGY_BANK_ID, group.piggyBanks().get(0).id());
    }

    @Test
    @TestTransaction
    void testGetPiggyBanksWithInvalidType() {
        // Act
        PiggyBanksRepresentation result = piggyBankService.getPiggyBanks(admin.getId(), "invalid_type");
        
        // Assert
        assertNotNull(result.personalPiggyBanks());
        assertTrue(result.personalPiggyBanks().isEmpty());
        assertNotNull(result.groupPiggyBanks());
        assertTrue(result.groupPiggyBanks().isEmpty());
    }

    @Test
    @TestTransaction
    void testGetPiggyBanksUserNotFound() {
        // Act & Assert
        assertThrows(
            NotFoundException.class,
            () -> piggyBankService.getPiggyBanks(999L, null)
        );
    }

    @Test
    @TestTransaction
    void testGetPiggyBanksWithAllocationAndVerifyCurrentAmount() {
        // Arrange - Get initial state
        PiggyBanksRepresentation initialState = piggyBankService.getPiggyBanks(admin.getId(), null);
        BigDecimal initialAmount = initialState.personalPiggyBanks().get(0).currentAmount();
        
        // Create allocation
        Long piggyBankId = initialState.personalPiggyBanks().get(0).id();
        BigDecimal allocationAmount = new BigDecimal("150.00");
        
        AllocateToPiggyBankCommand command = new AllocateToPiggyBankCommand(
            FIXED_DATE,
            new Money(allocationAmount),
            piggyBankId,
            admin.getId()
        );
        
        piggyBankAllocationService.allocateToPiggyBank(command);
        
        // Act - Get updated state
        PiggyBanksRepresentation updatedState = piggyBankService.getPiggyBanks(admin.getId(), null);
        BigDecimal updatedAmount = updatedState.personalPiggyBanks().get(0).currentAmount();
        
        // Assert - Verify current amount increased correctly
        assertEquals(initialAmount.add(allocationAmount), updatedAmount);
    }
}
