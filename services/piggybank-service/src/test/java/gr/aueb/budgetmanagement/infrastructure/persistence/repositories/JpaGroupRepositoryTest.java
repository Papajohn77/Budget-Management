package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.repositories.GroupRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.User;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
class JpaGroupRepositoryTest {
    private static final String NEW_GROUP_NAME = "Test Group";
    private static final String EXISTING_GROUP_NAME = "testgroup";

    @Inject
    private EntityManager entityManager;

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    private User user;
    private User noMember;
    private User alreadyMember;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
        noMember = userRepository.findById(Fixture.Users.TESTUSER2_ID).orElseThrow();
        alreadyMember = userRepository.findById(Fixture.Users.TESTUSER3_ID).orElseThrow();
    }

    @Test
    @TestTransaction
    void saveShouldPersistGroup() {
        // Arrange
        Group group = Group.create(NEW_GROUP_NAME, user);

        // Act
        groupRepository.save(group);

        // Assert
        Group foundGroup = entityManager.find(Group.class, group.getId());
        assertNotNull(foundGroup);
        assertEquals(NEW_GROUP_NAME, foundGroup.getName());
        assertEquals(user.getId(), foundGroup.getAdmin().getId());
        assertTrue(foundGroup.getMembers().contains(user));
    }

    @Test
    @TestTransaction
    void existsByNameAndMemberIdWhenGroupExistsShouldReturnTrue() {
        // Act
        boolean exists = groupRepository.existsByNameAndMemberId(EXISTING_GROUP_NAME, user.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    @TestTransaction
    void existsByNameAndMemberIdWhenGroupDoesNotExistShouldReturnFalse() {
        // Act
        boolean exists = groupRepository.existsByNameAndMemberId("Non Existing Group", user.getId());

        // Assert
        assertFalse(exists);
    }

    @Test
    @TestTransaction
    void existsByNameAndMemberIdWhenUserNotMemberShouldReturnFalse() {
        // Act
        boolean exists = groupRepository.existsByNameAndMemberId(EXISTING_GROUP_NAME, noMember.getId());

        // Assert
        assertFalse(exists);
    }

    @Test
    @TestTransaction
    void existsByNameAndMemberIdWithMultipleMembersShouldReturnTrue() {
        // Act
        boolean exists = groupRepository.existsByNameAndMemberId(EXISTING_GROUP_NAME, alreadyMember.getId());

        // Assert
        assertTrue(exists);
    }

    @Test
    @TestTransaction
    void testFindByIdExistingGroup() {
        Optional<Group> found = groupRepository.findById(Fixture.Groups.TESTGROUP_ID);
        assertTrue(found.isPresent());
    }

    @Test
    @TestTransaction
    void testFindByIdNonexistentUser() {
        Long nonExistentGroupId = 999L;
        Optional<Group> found = groupRepository.findById(nonExistentGroupId);
        
        assertFalse(found.isPresent());
    }
}
