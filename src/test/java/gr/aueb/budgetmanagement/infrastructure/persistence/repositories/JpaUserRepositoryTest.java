package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class JpaUserRepositoryTest {
    private static final String TEST_PASSWORD = "Test123!@#";

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

        assertTrue(repository.existsByEmail(user.getEmail().getValue()));
        assertFalse(repository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void testFindByEmailExistingUser() {
        User user = createTestUser();
        repository.save(user);

        Optional<User> found = repository.findByEmail(user.getEmail().getValue());

        assertTrue(found.isPresent());
        assertEquals(user.getEmail(), found.get().getEmail());
        assertEquals(user.getUsername(), found.get().getUsername());
    }

    @Test
    void testFindByIdExistingUser() {
        User user = createTestUser();
        repository.save(user);

        Optional<User> found = repository.findById(user.getId());

        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getId());
        assertEquals(user.getEmail(), found.get().getEmail());
        assertEquals(user.getUsername(), found.get().getUsername());
    }

    @Test
    void testFindByIdNonexistentUser() {
        Long nonexistentId = 999L;
        Optional<User> found = repository.findById(nonexistentId);
        
        assertFalse(found.isPresent());
    }

    private User createTestUser() {
        return User.create(
            "testuser",
            "test@example.com",
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );
    }
}
