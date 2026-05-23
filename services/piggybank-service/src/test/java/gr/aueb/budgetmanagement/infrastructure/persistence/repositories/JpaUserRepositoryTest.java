package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.ports.PasswordHasher;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
class JpaUserRepositoryTest {
    private static final String TEST_PASSWORD = "Test123!@#";

    @Inject
    private EntityManager entityManager;

    @Inject
    private UserRepository userRepository;

    @Inject
    private PasswordHasher passwordHasher;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
    }

    @Test
    @TestTransaction
    void testSaveUser() {
        User newUser = User.create(
            "newuser",
            "new@example.com",
            TEST_PASSWORD,
            passwordHasher
        );
        userRepository.save(newUser);

        assertNotNull(newUser.getId());
        assertTrue(entityManager.contains(newUser));
    }

    @Test
    @TestTransaction
    void testExistsByUsername() {
        assertTrue(userRepository.existsByUsername(user.getUsername()));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    @TestTransaction
    void testExistsByEmail() {
        assertTrue(userRepository.existsByEmail(user.getEmail().getValue()));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    @TestTransaction
    void testFindByEmailExistingUser() {
        Optional<User> found = userRepository.findByEmail(user.getEmail().getValue());

        assertTrue(found.isPresent());
        assertEquals(user.getEmail(), found.get().getEmail());
        assertEquals(user.getUsername(), found.get().getUsername());
    }

    @Test
    @TestTransaction
    void testFindByIdExistingUser() {
        Optional<User> found = userRepository.findById(user.getId());

        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getId());
        assertEquals(user.getEmail(), found.get().getEmail());
        assertEquals(user.getUsername(), found.get().getUsername());
    }

    @Test
    @TestTransaction
    void testFindByIdNonexistentUser() {
        Long nonexistentId = 999L;
        Optional<User> found = userRepository.findById(nonexistentId);
        
        assertFalse(found.isPresent());
    }
}
