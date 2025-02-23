package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.GroupPiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PersonalPiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PiggyBankAllocation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class JpaPiggyBankAllocationRepositoryTest {
    private EntityManager entityManager;
    private EntityTransaction transaction;
    private JpaPiggyBankAllocationRepository repository;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        repository = new JpaPiggyBankAllocationRepository(entityManager);
    }

    @AfterEach
    void tearDown() {
        if (transaction.isActive()) {
            transaction.rollback();
        }
        entityManager.close();
    }

    @Test
    void testSavePersonalPiggyBankAllocation() {
        PiggyBankAllocation allocation = createTestPersonalPiggyBankAllocation();
        repository.save(allocation);

        assertNotNull(allocation.getId());
        assertTrue(entityManager.contains(allocation));
    }

    @Test
    void testSaveGroupPiggyBankAllocation() {
        PiggyBankAllocation allocation = createTestGroupPiggyBankAllocation();
        repository.save(allocation);

        assertNotNull(allocation.getId());
        assertTrue(entityManager.contains(allocation));
    }

    private PiggyBankAllocation createTestPersonalPiggyBankAllocation() {
        User user = createTestUser();
        PersonalPiggyBank piggyBank = createTestPersonalPiggyBank(user);

        return PiggyBankAllocation.create(
            new Money(new BigDecimal("50.00")),
            LocalDate.now(),
            piggyBank,
            user
        );
    }

    private PiggyBankAllocation createTestGroupPiggyBankAllocation() {
        User user = createTestUser();
        Group group = createTestGroup(user);
        GroupPiggyBank piggyBank = createTestGroupPiggyBank(group);

        return PiggyBankAllocation.create(
            new Money(new BigDecimal("75.00")),
            LocalDate.now(),
            piggyBank,
            user
        );
    }

    private User createTestUser() {
        User user = User.create(
            "testuser",
            new EmailAddress("test@example.com"),
            "hashedPassword123"
        );
        entityManager.persist(user);
        return user;
    }

    private PersonalPiggyBank createTestPersonalPiggyBank(User user) {
        PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
            "Test Personal PiggyBank",
            new Money(new BigDecimal("1000.00")),
            ExpenseCategory.ENTERTAINMENT,
            user
        );
        entityManager.persist(piggyBank);
        return piggyBank;
    }

    private Group createTestGroup(User user) {
        Group group = Group.create("Test Group", user);
        entityManager.persist(group);
        return group;
    }

    private GroupPiggyBank createTestGroupPiggyBank(Group group) {
        GroupPiggyBank piggyBank = GroupPiggyBank.create(
            "Test Group PiggyBank",
            new Money(new BigDecimal("2000.00")),
            ExpenseCategory.ENTERTAINMENT,
            group
        );
        entityManager.persist(piggyBank);
        return piggyBank;
    }
}
