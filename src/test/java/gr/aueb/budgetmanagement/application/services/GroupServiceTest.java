package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.application.commands.CreateGroupCommand;
import gr.aueb.budgetmanagement.application.dto.CreatedGroupDTO;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.exceptions.GroupAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaGroupRepository;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaUserRepository;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class GroupServiceTest {
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Test123!@#";

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private JpaUserRepository userRepository;
    private JpaGroupRepository groupRepository;
    private GroupService groupService;
    private User user;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        userRepository = new JpaUserRepository(entityManager);
        groupRepository = new JpaGroupRepository(entityManager);
        groupService = new GroupService(groupRepository, userRepository);

        createTestUser();
    }

    @AfterEach
    void tearDown() {
        if (transaction.isActive()) {
            transaction.rollback();
        }
        entityManager.close();
    }

    private void createTestUser() {
        user = User.create(
            TEST_USERNAME,
            TEST_EMAIL,
            TEST_PASSWORD,
            new BCryptPasswordEncoder()
        );
        entityManager.persist(user);
    }

   @Test
   void createGroupWithValidData() {
       // Arrange
        CreateGroupCommand command = new CreateGroupCommand(
            "Test Group", 
            user.getId()
        );

       // Act
       CreatedGroupDTO result = groupService.createGroup(command);

       // Assert
       assertNotNull(result.id());
       assertEquals("Test Group", result.name());
       assertTrue(result.isAdmin());
       
       Group savedGroup = entityManager.find(Group.class, result.id());
       assertNotNull(savedGroup);
       assertEquals("Test Group", savedGroup.getName());
       assertEquals(user.getId(), savedGroup.getAdmin().getId());
       assertTrue(savedGroup.getMembers().contains(user));
   }

   @Test
   void createGroupWithNonExistentUser() {
       // Arrange
        CreateGroupCommand command = new CreateGroupCommand(
            "Test Group", 
            999L
        );

       // Act & Assert
        assertThrows(
            NotFoundException.class,
            () -> groupService.createGroup(command)
        );
   }

   @Test
   void createGroupWithExistingGroupName() {
       // Arrange
        CreateGroupCommand command = new CreateGroupCommand(
            "Test Group", 
            user.getId()
        );
       groupService.createGroup(command);

        // Act & Assert
        assertThrows(
            GroupAlreadyExistsException.class,
            () -> groupService.createGroup(command)
        );
   }

   @Test
   void createGroupWithInvalidCommand() {
       // Arrange
       CreateGroupCommand command = new CreateGroupCommand("", user.getId());

       // Act & Assert
       assertThrows(
           InvalidDomainArgumentException.class,
           () -> groupService.createGroup(command)
       );
   }
}
