package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.application.commands.CreateGroupPiggyBankCommand;
import gr.aueb.budgetmanagement.application.commands.CreatePersonalPiggyBankCommand;
import gr.aueb.budgetmanagement.application.exceptions.ForbiddenException;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.repositories.GroupRepository;
import gr.aueb.budgetmanagement.domain.repositories.PiggyBankRepository;
import gr.aueb.budgetmanagement.domain.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaGroupRepository;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaPiggyBankRepository;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaUserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class PiggyBankServiceTest {
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_GROUP_NAME = "testgroup";
    private static final BigDecimal TARGET_AMOUNT = new BigDecimal("1000.00");
    private static final String PIGGY_BANK_NAME = "Test Piggy Bank";

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private UserRepository userRepository;
    private GroupRepository groupRepository;
    private PiggyBankRepository piggyBankRepository;
    private PiggyBankService piggyBankService;
    private User user;
    private Group group;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        userRepository = new JpaUserRepository(entityManager);
        groupRepository = new JpaGroupRepository(entityManager);
        piggyBankRepository = new JpaPiggyBankRepository(entityManager);
        piggyBankService = new PiggyBankService(userRepository, groupRepository, piggyBankRepository);

        createTestUser();
        createTestGroup();
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
            new EmailAddress(TEST_EMAIL),
            TEST_PASSWORD
        );
        entityManager.persist(user);
    }

    private void createTestGroup() {
        group = Group.create(TEST_GROUP_NAME, user);
        entityManager.persist(group);
    }

    @Test
    void testCreatePersonalPiggyBank() {
        CreatePersonalPiggyBankCommand command = new CreatePersonalPiggyBankCommand(
            PIGGY_BANK_NAME,
            new Money(TARGET_AMOUNT),
            ExpenseCategory.OTHER,
            user.getId()
        );

        var result = piggyBankService.createPersonalPiggyBank(command);

        assertNotNull(result.id());
        assertEquals(PIGGY_BANK_NAME, result.name());
        assertEquals(TARGET_AMOUNT, result.targetAmount().getValue());
        assertEquals(ExpenseCategory.OTHER, result.category());

        User persistedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(1, persistedUser.getPiggyBanks().size());
    }

    @Test
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
    void testCreateGroupPiggyBank() {
        CreateGroupPiggyBankCommand command = new CreateGroupPiggyBankCommand(
            PIGGY_BANK_NAME,
            new Money(TARGET_AMOUNT),
            ExpenseCategory.OTHER,
            group.getId(),
            user.getId()
        );

        var result = piggyBankService.createGroupPiggyBank(command);

        assertNotNull(result.id());
        assertEquals(PIGGY_BANK_NAME, result.name());
        assertEquals(TARGET_AMOUNT, result.targetAmount().getValue());
        assertEquals(ExpenseCategory.OTHER, result.category());
        assertEquals(group.getId(), result.groupId());

        Group persistedGroup = groupRepository.findById(group.getId()).orElseThrow();
        assertEquals(1, persistedGroup.getPiggyBanks().size());
    }

    @Test
    void testCreateGroupPiggyBankGroupNotFound() {
        CreateGroupPiggyBankCommand command = new CreateGroupPiggyBankCommand(
            PIGGY_BANK_NAME,
            new Money(TARGET_AMOUNT),
            ExpenseCategory.OTHER,
            999L,
            user.getId()
        );

        assertThrows(
            NotFoundException.class,
            () -> piggyBankService.createGroupPiggyBank(command)
        );
    }

    @Test
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
    void testCreateGroupPiggyBankNotAdmin() {
        User nonAdmin = User.create("other", new EmailAddress("other@example.com"), "password");
        entityManager.persist(nonAdmin);

        CreateGroupPiggyBankCommand command = new CreateGroupPiggyBankCommand(
            PIGGY_BANK_NAME,
            new Money(TARGET_AMOUNT),
            ExpenseCategory.OTHER,
            group.getId(),
            nonAdmin.getId()
        );

        assertThrows(
            ForbiddenException.class,
            () -> piggyBankService.createGroupPiggyBank(command)
        );
    }
}
