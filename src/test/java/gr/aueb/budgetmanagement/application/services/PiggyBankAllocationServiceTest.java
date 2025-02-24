package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.application.commands.AllocateToPiggyBankCommand;
import gr.aueb.budgetmanagement.application.dto.PiggyBankAllocationDTO;
import gr.aueb.budgetmanagement.application.exceptions.ForbiddenException;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.GroupPiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PersonalPiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PiggyBank;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.repositories.PiggyBankAllocationRepository;
import gr.aueb.budgetmanagement.domain.repositories.PiggyBankRepository;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaPiggyBankAllocationRepository;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaPiggyBankRepository;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class PiggyBankAllocationServiceTest {
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password";
    private static final LocalDate TODAY = LocalDate.now();
    private static final BigDecimal AMOUNT = new BigDecimal("100.00");
    
    private EntityManager entityManager;
    private EntityTransaction transaction;
    private UserRepository userRepository;
    private PiggyBankRepository piggyBankRepository;
    private PiggyBankAllocationRepository allocationRepository;
    private PiggyBankAllocationService allocationService;
    private User user;
    private PersonalPiggyBank personalPiggyBank;
    private GroupPiggyBank groupPiggyBank;
    private Group group;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();
        
        userRepository = new JpaUserRepository(entityManager);
        piggyBankRepository = new JpaPiggyBankRepository(entityManager);
        allocationRepository = new JpaPiggyBankAllocationRepository(entityManager);
        
        allocationService = new PiggyBankAllocationService(
            allocationRepository,
            piggyBankRepository,
            userRepository
        );
        
        createTestEntities();
    }

    @AfterEach
    void tearDown() {
        if (transaction.isActive()) {
            transaction.rollback();
        }
        entityManager.close();
    }

    private void createTestEntities() {
        user = User.create(
            TEST_USERNAME,
            new EmailAddress(TEST_EMAIL),
            TEST_PASSWORD
        );
        entityManager.persist(user);

        personalPiggyBank = PersonalPiggyBank.create(
            "Personal Savings",
            new Money(new BigDecimal("1000.00")),
            ExpenseCategory.ENTERTAINMENT,
            user
        );
        entityManager.persist(personalPiggyBank);

        group = Group.create("Test Group", user);
        entityManager.persist(group);

        groupPiggyBank = GroupPiggyBank.create(
            "Group Savings",
            new Money(new BigDecimal("2000.00")),
            ExpenseCategory.ENTERTAINMENT,
            group
        );
        entityManager.persist(groupPiggyBank);
    }

    @Test
    void testSuccessfulPersonalPiggyBankAllocation() {
        // Arrange
        AllocateToPiggyBankCommand command = new AllocateToPiggyBankCommand(
            TODAY,
            new Money(AMOUNT),
            personalPiggyBank.getId(),
            user.getId()
        );

        // Act
        PiggyBankAllocationDTO result = allocationService.allocateToPiggyBank(command);

        // Assert
        assertNotNull(result.id());
        assertEquals(TODAY, result.date());
        assertEquals(AMOUNT, result.amount().getValue());
        assertEquals(personalPiggyBank.getId(), result.piggyBankId());

        // Verify persistence
        PiggyBank persistedPiggyBank = piggyBankRepository.findById(personalPiggyBank.getId()).orElseThrow();
        assertEquals(1, persistedPiggyBank.getAllocations().size());
    }

    @Test
    void testSuccessfulGroupPiggyBankAllocation() {
        // Arrange
        AllocateToPiggyBankCommand command = new AllocateToPiggyBankCommand(
            TODAY,
            new Money(AMOUNT),
            groupPiggyBank.getId(),
            user.getId()
        );

        // Act
        PiggyBankAllocationDTO result = allocationService.allocateToPiggyBank(command);

        // Assert
        assertNotNull(result.id());
        assertEquals(TODAY, result.date());
        assertEquals(AMOUNT, result.amount().getValue());
        assertEquals(groupPiggyBank.getId(), result.piggyBankId());

        // Verify persistence
        PiggyBank persistedPiggyBank = piggyBankRepository.findById(groupPiggyBank.getId()).orElseThrow();
        assertEquals(1, persistedPiggyBank.getAllocations().size());
    }

    @Test
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
    void testAllocationToUnauthorizedPersonalPiggyBank() {
        // Arrange
        User unauthorizedUser = User.create(
            "unauthorized",
            new EmailAddress("unauthorized@example.com"),
            "hashedPassword123"
        );
        entityManager.persist(unauthorizedUser);

        AllocateToPiggyBankCommand command = new AllocateToPiggyBankCommand(
            TODAY,
            new Money(AMOUNT),
            personalPiggyBank.getId(),
            unauthorizedUser.getId()
        );

        // Act & Assert
        assertThrows(
            ForbiddenException.class,
            () -> allocationService.allocateToPiggyBank(command)
        );
    }

    @Test
    void testAllocationToUnauthorizedGroupPiggyBank() {
        // Arrange
        User nonMember = User.create(
            "nonmember",
            new EmailAddress("nonmember@example.com"),
            "password"
        );
        entityManager.persist(nonMember);

        AllocateToPiggyBankCommand command = new AllocateToPiggyBankCommand(
            TODAY,
            new Money(AMOUNT),
            groupPiggyBank.getId(),
            nonMember.getId()
        );

        // Act & Assert
        assertThrows(
            ForbiddenException.class,
            () -> allocationService.allocateToPiggyBank(command)
        );
    }
}
