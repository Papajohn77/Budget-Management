package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class JpaUserRepositoryTest {
    private EntityManager entityManager;
    private EntityTransaction transaction;
    private JpaUserRepository repository;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        repository = new JpaUserRepository(entityManager);
    }

    @AfterEach
    void tearDown() {
        if (transaction.isActive()) {
            transaction.rollback();
        }
        entityManager.close();
    }

    @Test
    void testSaveUser() {
        User user = createTestUser();
        repository.save(user);
        
        assertNotNull(user.getId());
        assertTrue(entityManager.contains(user));
    }

    @Test
    void testExistsByUsername() {
        User user = createTestUser();
        repository.save(user);

        assertTrue(repository.existsByUsername(user.getUsername()));
        assertFalse(repository.existsByUsername("nonexistent"));
    }

    @Test
    void testExistsByEmail() {
        User user = createTestUser();
        repository.save(user);

        assertTrue(repository.existsByEmail(user.getEmail()));
        assertFalse(repository.existsByEmail(new EmailAddress("nonexistent@example.com")));
    }

    private User createTestUser() {
        return User.create(
            "testuser",
            new EmailAddress("test@example.com"),
            "hashedPassword123"
        );
    }
}
