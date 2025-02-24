package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.GroupPiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PersonalPiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PiggyBankAllocation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class JpaPiggyBankRepositoryTest {
    private static final String TEST_PASSWORD = "Test123!@#";

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private JpaPiggyBankRepository repository;
    private User user;
    private Group group;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();
        repository = new JpaPiggyBankRepository(entityManager);

        user = createTestUser();
        group = createTestGroup(user);
    }

    @AfterEach
    void tearDown() {
        if (transaction.isActive()) {
            transaction.rollback();
        }
        entityManager.close();
    }

    @Test
    void testSavePersonalPiggyBank() {
        PersonalPiggyBank piggyBank = createTestPersonalPiggyBank(user);
        repository.save(piggyBank);

        assertNotNull(piggyBank.getId());
        assertTrue(entityManager.contains(piggyBank));
        assertTrue(user.getPiggyBanks().contains(piggyBank));
    }

    @Test
    void testSaveGroupPiggyBank() {
        GroupPiggyBank piggyBank = createTestGroupPiggyBank(group);
        repository.save(piggyBank);

        assertNotNull(piggyBank.getId());
        assertTrue(entityManager.contains(piggyBank));
        assertTrue(group.getPiggyBanks().contains(piggyBank));
    }

    @Test
    void testFindByIdExistingPersonalPiggyBank() {
        PersonalPiggyBank piggyBank = createTestPersonalPiggyBank(user);
        repository.save(piggyBank);

        Optional<PiggyBank> found = repository.findById(piggyBank.getId());

        assertTrue(found.isPresent());
        PiggyBank foundPiggyBank = found.get();
        assertTrue(foundPiggyBank instanceof PersonalPiggyBank);
        assertEquals(piggyBank.getId(), foundPiggyBank.getId());
        assertEquals(piggyBank.getName(), foundPiggyBank.getName());
        assertEquals(piggyBank.getTargetAmount(), foundPiggyBank.getTargetAmount());
        assertEquals(piggyBank.getCategory(), foundPiggyBank.getCategory());
        assertEquals(user, ((PersonalPiggyBank) foundPiggyBank).getUser());
    }

    @Test
    void testFindByIdExistingGroupPiggyBank() {
        GroupPiggyBank piggyBank = createTestGroupPiggyBank(group);
        repository.save(piggyBank);

        Optional<PiggyBank> found = repository.findById(piggyBank.getId());

        assertTrue(found.isPresent());
        PiggyBank foundPiggyBank = found.get();
        assertTrue(foundPiggyBank instanceof GroupPiggyBank);
        assertEquals(piggyBank.getId(), foundPiggyBank.getId());
        assertEquals(piggyBank.getName(), foundPiggyBank.getName());
        assertEquals(piggyBank.getTargetAmount(), foundPiggyBank.getTargetAmount());
        assertEquals(piggyBank.getCategory(), foundPiggyBank.getCategory());
        assertEquals(group, ((GroupPiggyBank) foundPiggyBank).getGroup());
    }

    @Test
    void testFindByIdNonexistentPiggyBank() {
        Long nonexistentId = 999L;
        Optional<PiggyBank> found = repository.findById(nonexistentId);

        assertFalse(found.isPresent());
    }

    @Test
    void testDeletePersonalPiggyBank() {
        // Arrange
        PersonalPiggyBank piggyBank = createTestPersonalPiggyBank(user);
        repository.save(piggyBank);

        PiggyBankAllocation allocation = PiggyBankAllocation.create(
            new Money(BigDecimal.valueOf(500)),
            LocalDate.now(),
            piggyBank,
            user
        );
        entityManager.persist(allocation);
        
        // Act
        repository.delete(piggyBank);

        // Assert
        assertNull(entityManager.find(PiggyBank.class, piggyBank.getId()));
        assertNull(entityManager.find(PiggyBankAllocation.class, allocation.getId()));
    }

    @Test
    void testDeleteGroupPiggyBank() {
        // Arrange
        GroupPiggyBank piggyBank = createTestGroupPiggyBank(group);
        repository.save(piggyBank);

        PiggyBankAllocation allocation = PiggyBankAllocation.create(
            new Money(BigDecimal.valueOf(500)),
            LocalDate.now(),
            piggyBank,
            user
        );
        entityManager.persist(allocation);
        
        // Act
        repository.delete(piggyBank);

        // Assert
        assertNull(entityManager.find(PiggyBank.class, piggyBank.getId()));
        assertNull(entityManager.find(PiggyBankAllocation.class, allocation.getId()));
    }

    private User createTestUser() {
        User user = User.create(
            "testuser",
            "test@example.com",
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );

        entityManager.persist(user);

        return user;
    }

    private Group createTestGroup(User admin) {
        Group group = Group.create("testgroup", admin);
        entityManager.persist(group);
        return group;
    }

    private PersonalPiggyBank createTestPersonalPiggyBank(User user) {
        return PersonalPiggyBank.create(
            "Vacation fund", 
            new Money(BigDecimal.valueOf(1000)),
            ExpenseCategory.ENTERTAINMENT,
            user
        );
    }

    private GroupPiggyBank createTestGroupPiggyBank(Group group) {
        return GroupPiggyBank.create(
            "Family vacation",
            new Money(BigDecimal.valueOf(2000)),
            ExpenseCategory.ENTERTAINMENT,
            group
        );
    }
}
