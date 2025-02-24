package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.entities.SavingsOperation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.SavingsOperationType;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class JpaSavingsOperationRepositoryTest {
    private static final String TEST_PASSWORD = "Test123!@#";

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private JpaSavingsOperationRepository repository;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        repository = new JpaSavingsOperationRepository(entityManager);
    }

    @AfterEach
    void tearDown() {
        if (transaction.isActive()) {
            transaction.rollback();
        }
        entityManager.close();
    }

    @Test
    void testSaveAllocationSavingsOperation() {
        SavingsOperation savingsOperation = createTestAllocationSavingsOperation();
        repository.save(savingsOperation);
        
        assertNotNull(savingsOperation.getId());
        assertTrue(entityManager.contains(savingsOperation));
    }

    @Test
    void testSaveDeallocationSavingsOperation() {
        SavingsOperation savingsOperation = createTestDeallocationSavingsOperation();
        repository.save(savingsOperation);

        assertNotNull(savingsOperation.getId());
        assertTrue(entityManager.contains(savingsOperation));
    }

    private SavingsOperation createTestAllocationSavingsOperation() {
        User user = createTestUser();

        return SavingsOperation.create(
            new Money(new BigDecimal("100.00")),
            LocalDate.now(),
            SavingsOperationType.ALLOCATION,
            user.getSavings()
        );
    }

    private SavingsOperation createTestDeallocationSavingsOperation() {
        User user = createTestUser();

        return SavingsOperation.create(
            new Money(new BigDecimal("100.00")),
            LocalDate.now(),
            SavingsOperationType.DEALLOCATION,
            user.getSavings()
        );
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
}
