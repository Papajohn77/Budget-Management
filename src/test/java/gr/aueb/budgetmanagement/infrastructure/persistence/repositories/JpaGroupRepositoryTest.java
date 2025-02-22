package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class JpaGroupRepositoryTest {
   private EntityManager entityManager;
   private EntityTransaction transaction;
   private JpaUserRepository userRepository;
   private JpaGroupRepository groupRepository;
   private User user;

   @BeforeEach
   void setUp() {
       entityManager = JPAUtil.getCurrentEntityManager();
       transaction = entityManager.getTransaction();
       transaction.begin();

       userRepository = new JpaUserRepository(entityManager);
       groupRepository = new JpaGroupRepository(entityManager);

       user = createTestUser();
       userRepository.save(user);
   }

   @AfterEach
   void tearDown() {
       if (transaction.isActive()) {
           transaction.rollback();
       }
       entityManager.close();
   }

   @Test
   void saveShouldPersistGroup() {
       // Arrange
       Group group = Group.create("Test Group", user);

       // Act
       groupRepository.save(group);

       // Assert
       Group foundGroup = entityManager.find(Group.class, group.getId());
       assertNotNull(foundGroup);
       assertEquals("Test Group", foundGroup.getName());
       assertEquals(user.getId(), foundGroup.getAdmin().getId());
       assertTrue(foundGroup.getMembers().contains(user));
   }

   @Test
   void existsByNameAndMemberIdWhenGroupExistsShouldReturnTrue() {
       // Arrange
       Group group = Group.create("Test Group", user);
       groupRepository.save(group);

       // Act
       boolean exists = groupRepository.existsByNameAndMemberId("Test Group", user.getId());

       // Assert
       assertTrue(exists);
   }

   @Test
   void existsByNameAndMemberIdWhenGroupDoesNotExistShouldReturnFalse() {
       // Act
       boolean exists = groupRepository.existsByNameAndMemberId("Non Existing Group", user.getId());

       // Assert
       assertFalse(exists);
   }

   @Test
   void existsByNameAndMemberIdWhenUserNotMemberShouldReturnFalse() {
       // Arrange
       User otherUser = createOtherTestUser();
       entityManager.persist(otherUser);
       
       Group group = Group.create("Test Group", user);
       groupRepository.save(group);

       // Act
       boolean exists = groupRepository.existsByNameAndMemberId("Test Group", otherUser.getId());

       // Assert
       assertFalse(exists);
   }

   @Test
   void existsByNameAndMemberIdWithMultipleMembersShouldReturnTrue() {
       // Arrange
       User otherUser = createOtherTestUser();
       entityManager.persist(otherUser);
       
       Group group = Group.create("Test Group", user);
       group.addMember(otherUser);
       groupRepository.save(group);

       // Act
       boolean exists = groupRepository.existsByNameAndMemberId("Test Group", otherUser.getId());

       // Assert
       assertTrue(exists);
   }

    @Test
    void testFindByIdExistingGroup() {
        Group group = Group.create("Test Group", user);
        groupRepository.save(group);
        
        Optional<Group> found = groupRepository.findById(group.getId());
        
        assertTrue(found.isPresent());
        assertEquals(group.getId(), found.get().getId());
        assertEquals(group.getName(), found.get().getName());
        assertEquals(group.getAdmin(), found.get().getAdmin());
        assertTrue(found.get().getMembers().contains(user));
    }

    @Test
    void testFindByIdNonexistentUser() {
        Long nonexistentId = 999L;
        Optional<Group> found = groupRepository.findById(nonexistentId);
        
        assertFalse(found.isPresent());
    }

    private User createTestUser() {
        return User.create(
            "testuser",
            new EmailAddress("test@example.com"),
            "hashedPassword123"
        );
    }

    private User createOtherTestUser() {
        return User.create(
            "otheruser", 
            new EmailAddress("other@example.com"), 
            "hashedPassword123");
    }
}
