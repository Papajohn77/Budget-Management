package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.repositories.GroupRepository;
import gr.aueb.budgetmanagement.application.repositories.PiggyBankRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.GroupPiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PersonalPiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PiggyBank;
import gr.aueb.budgetmanagement.domain.entities.PiggyBankAllocation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
class JpaPiggyBankRepositoryTest {

    @Inject
    private EntityManager entityManager;

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private PiggyBankRepository piggyBankRepository;

    private User user;
    private Group group;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
        group = groupRepository.findById(Fixture.Groups.TESTGROUP_ID).orElseThrow();
    }

    @Test
    @TestTransaction
    void testSavePersonalPiggyBank() {
        PersonalPiggyBank piggyBank = PersonalPiggyBank.create(
            "Vacation fund",
            new Money(BigDecimal.valueOf(1000)),
            ExpenseCategory.ENTERTAINMENT,
            user
        );
        piggyBankRepository.save(piggyBank);

        assertNotNull(piggyBank.getId());
        assertTrue(entityManager.contains(piggyBank));
        assertTrue(user.getPiggyBanks().contains(piggyBank));
    }

    @Test
    @TestTransaction
    void testSaveGroupPiggyBank() {
        GroupPiggyBank piggyBank = GroupPiggyBank.create(
            "Family vacation",
            new Money(BigDecimal.valueOf(2000)),
            ExpenseCategory.ENTERTAINMENT,
            group,
            group.getAdmin()
        );
        piggyBankRepository.save(piggyBank);

        assertNotNull(piggyBank.getId());
        assertTrue(entityManager.contains(piggyBank));
        assertTrue(group.getPiggyBanks().contains(piggyBank));
    }

    @Test
    @TestTransaction
    void testFindByIdExistingPersonalPiggyBank() {
        Optional<PiggyBank> found = piggyBankRepository.findById(Fixture.PiggyBanks.PERSONAL_PIGGY_BANK_ID);
        assertTrue(found.isPresent());

        PiggyBank foundPiggyBank = found.get();
        assertTrue(foundPiggyBank instanceof PersonalPiggyBank);
    }

    @Test
    @TestTransaction
    void testFindByIdExistingGroupPiggyBank() {
        Optional<PiggyBank> found = piggyBankRepository.findById(Fixture.PiggyBanks.GROUP_PIGGY_BANK_ID);
        assertTrue(found.isPresent());

        PiggyBank foundPiggyBank = found.get();
        assertTrue(foundPiggyBank instanceof GroupPiggyBank);
    }

    @Test
    @TestTransaction
    void testFindByIdNonexistentPiggyBank() {
        Long nonexistentId = 999L;
        Optional<PiggyBank> found = piggyBankRepository.findById(nonexistentId);

        assertFalse(found.isPresent());
    }

    @Test
    @TestTransaction
    void testDeletePersonalPiggyBank() {
        // Arrange
        PersonalPiggyBank personalPiggyBank = (PersonalPiggyBank) piggyBankRepository
                .findById(Fixture.PiggyBanks.PERSONAL_PIGGY_BANK_ID)
                .orElseThrow();

        PiggyBankAllocation allocation = personalPiggyBank.allocate(
            new Money(BigDecimal.valueOf(500)),
            LocalDate.now(), 
            user
        );
        entityManager.persist(allocation);
        
        // Act
        piggyBankRepository.delete(personalPiggyBank);

        // Assert
        assertNull(entityManager.find(PiggyBank.class, personalPiggyBank.getId()));
        assertNull(entityManager.find(PiggyBankAllocation.class, allocation.getId()));
    }

    @Test
    @TestTransaction
    void testDeleteGroupPiggyBank() {
        // Arrange
        GroupPiggyBank groupPiggyBank = (GroupPiggyBank) piggyBankRepository
                .findById(Fixture.PiggyBanks.GROUP_PIGGY_BANK_ID)
                .orElseThrow();

        PiggyBankAllocation allocation = groupPiggyBank.allocate(
            new Money(BigDecimal.valueOf(500)),
            LocalDate.now(), 
            user
        );
        entityManager.persist(allocation);
        
        // Act
        piggyBankRepository.delete(groupPiggyBank);

        // Assert
        assertNull(entityManager.find(PiggyBank.class, groupPiggyBank.getId()));
        assertNull(entityManager.find(PiggyBankAllocation.class, allocation.getId()));
    }
}
