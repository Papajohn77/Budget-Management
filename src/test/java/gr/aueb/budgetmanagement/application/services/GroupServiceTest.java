package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.commands.CreateGroupCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.GroupRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.CreatedGroupRepresentation;
import gr.aueb.budgetmanagement.application.representations.GroupsRepresentation;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.exceptions.GroupAlreadyExistsException;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;

@QuarkusTest
class GroupServiceTest {
    private static final String TEST_GROUP_NAME = "Other Test Group";

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private GroupService groupService;

    private User user;

    @BeforeEach
    void setUp() {
        user = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
    }

   @Test
   @TestTransaction
   void createGroupWithValidData() {
       // Arrange
        CreateGroupCommand command = new CreateGroupCommand(
            TEST_GROUP_NAME, 
            user.getId()
        );

       // Act
       CreatedGroupRepresentation result = groupService.createGroup(command);

       // Assert
       assertNotNull(result.id());
       assertEquals(TEST_GROUP_NAME, result.name());
       assertTrue(result.isAdmin());
       
       Group savedGroup = groupRepository.findById(result.id()).orElseThrow();
       assertNotNull(savedGroup);
       assertEquals(TEST_GROUP_NAME, savedGroup.getName());
       assertEquals(user.getId(), savedGroup.getAdmin().getId());
       assertTrue(savedGroup.getMembers().contains(user));
   }

   @Test
   @TestTransaction
   void createGroupWithNonExistentUser() {
       // Arrange
        CreateGroupCommand command = new CreateGroupCommand(
            TEST_GROUP_NAME, 
            999L
        );

       // Act & Assert
        assertThrows(
            NotFoundException.class,
            () -> groupService.createGroup(command)
        );
   }

   @Test
   @TestTransaction
   void createGroupWithExistingGroupName() {
       // Arrange
        CreateGroupCommand command = new CreateGroupCommand(
            TEST_GROUP_NAME, 
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
   @TestTransaction
   void createGroupWithInvalidCommand() {
       // Arrange
       CreateGroupCommand command = new CreateGroupCommand("", user.getId());

       // Act & Assert
       assertThrows(
            ConstraintViolationException.class,
           () -> groupService.createGroup(command)
       );
   }

    @Test
    @TestTransaction
    void getGroupsForExistingUser() {
        // Act
        GroupsRepresentation result = groupService.getGroups(user.getId());
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.groups());
        assertEquals(1, result.groups().size());
        
        CreatedGroupRepresentation groupRepresentation = result.groups().get(0);
        assertEquals(Fixture.Groups.TESTGROUP_ID, groupRepresentation.id());
        assertEquals("testgroup", groupRepresentation.name());
        assertTrue(groupRepresentation.isAdmin());
    }

    @Test
    @TestTransaction
    void getGroupsForUserWithNoGroups() {
        // Arrange - Use testuser2 who is not in any group based on test data
        User userWithNoGroups = userRepository.findById(Fixture.Users.TESTUSER2_ID).orElseThrow();
        
        // Act
        GroupsRepresentation result = groupService.getGroups(userWithNoGroups.getId());
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.groups());
        assertEquals(0, result.groups().size());
    }

    @Test
    @TestTransaction
    void getGroupsForUserWhoIsGroupMemberButNotAdmin() {
        // Arrange - Use testuser3 who is member but not admin of testgroup
        User memberUser = userRepository.findById(Fixture.Users.TESTUSER3_ID).orElseThrow();
        
        // Act
        GroupsRepresentation result = groupService.getGroups(memberUser.getId());
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.groups());
        assertEquals(1, result.groups().size());
        
        CreatedGroupRepresentation groupRepresentation = result.groups().get(0);
        assertEquals(Fixture.Groups.TESTGROUP_ID, groupRepresentation.id());
        assertEquals("testgroup", groupRepresentation.name());
        assertFalse(groupRepresentation.isAdmin());
    }

    @Test
    @TestTransaction
    void getGroupsForNonExistentUser() {
        // Act & Assert
        assertThrows(
            NotFoundException.class,
            () -> groupService.getGroups(999L)
        );
    }
}
