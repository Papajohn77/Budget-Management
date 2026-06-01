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
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
class JpaUserRepositoryTest {
    @Inject
    private EntityManager entityManager;

    @Inject
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
    }

    @Test
    @TestTransaction
    void testSaveUser() {
        User newUser = User.create(99L);
        userRepository.save(newUser);

        assertNotNull(newUser.getId());
        assertTrue(entityManager.contains(newUser));
    }

    @Test
    @TestTransaction
    void testFindByIdExistingUser() {
        Optional<User> found = userRepository.findById(user.getId());

        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getId());
    }

    @Test
    @TestTransaction
    void testFindByIdNonexistentUser() {
        Long nonexistentId = 999L;
        Optional<User> found = userRepository.findById(nonexistentId);
        
        assertFalse(found.isPresent());
    }
}
