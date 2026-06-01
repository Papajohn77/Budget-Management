package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.commands.RespondToInvitationCommand;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.application.repositories.GroupRepository;
import gr.aueb.budgetmanagement.application.repositories.InvitationRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.application.representations.InvitationRepresentation;
import gr.aueb.budgetmanagement.application.representations.InvitationsRepresentation;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.Invitation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.InvitationResponseOperationType;
import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;
import gr.aueb.budgetmanagement.domain.valueobjects.InvitationId;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class InvitationServiceTest {
    // private static final String INVITEE_EMAIL = "test4@example.com";

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private InvitationRepository invitationRepository;

    @Inject
    private InvitationService invitationService;

    private User admin;
    private User alreadyInvited;
    private User alreadyMember;
    private User newInvitee;
    private Group group;

    @BeforeEach
    void setUp() {
        admin = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
        alreadyInvited = userRepository.findById(Fixture.Users.TESTUSER2_ID).orElseThrow();
        alreadyMember = userRepository.findById(Fixture.Users.TESTUSER3_ID).orElseThrow();
        newInvitee = userRepository.findById(Fixture.Users.TESTUSER4_ID).orElseThrow();
        group = groupRepository.findById(Fixture.Groups.TESTGROUP_ID).orElseThrow();
    }

    // @Test
    // @TestTransaction
    // void sendInvitation_WithValidData_ShouldCreateInvitation() {
    //     // Arrange
    //     SendInvitationCommand command = new SendInvitationCommand(
    //         group.getId(),
    //         INVITEE_EMAIL,
    //         admin.getId()
    //     );

    //     // Act
    //     InvitationRepresentation result = invitationService.sendInvitation(command);

    //     // Assert
    //     assertNotNull(result);
    //     assertEquals(group.getId(), result.groupId());
    //     assertEquals(newInvitee.getId(), result.inviteeId());
    //     assertEquals(InvitationStatus.PENDING, result.status());
    //     assertNotNull(result.createdAt());

    //     // Verify the invitation was persisted
    //     InvitationId invitationId = new InvitationId(group.getId(), newInvitee.getId());
    //     Invitation savedInvitation = invitationRepository.findById(invitationId).orElseThrow();

    //     assertNotNull(savedInvitation);
    //     assertEquals(group.getId(), savedInvitation.getGroup().getId());
    //     assertEquals(newInvitee.getId(), savedInvitation.getInvitee().getId());
    //     assertEquals(InvitationStatus.PENDING, savedInvitation.getStatus());
    //     assertNotNull(savedInvitation.getCreatedAt());

    //     // Verify the invitee has the invitation in their collection
    //     User invitee = userRepository.findById(newInvitee.getId()).orElseThrow();
    //     assertTrue(invitee.getInvitations().contains(savedInvitation));
    // }

    // @Test
    // @TestTransaction
    // void sendInvitation_WithNonExistentGroup_ShouldThrowNotFoundException() {
    //     // Arrange
    //     SendInvitationCommand command = new SendInvitationCommand(
    //         999L,
    //         INVITEE_EMAIL,
    //         admin.getId()
    //     );

    //     // Act & Assert
    //     NotFoundException exception = assertThrows(
    //         NotFoundException.class,
    //         () -> invitationService.sendInvitation(command)
    //     );
    //     assertEquals("Group not found with id: 999", exception.getMessage());
    // }

    // @Test
    // @TestTransaction
    // void sendInvitation_WithNonExistentUser_ShouldThrowNotFoundException() {
    //     // Arrange
    //     SendInvitationCommand command = new SendInvitationCommand(
    //         group.getId(),
    //         INVITEE_EMAIL,
    //         999L
    //     );

    //     // Act & Assert
    //     NotFoundException exception = assertThrows(
    //         NotFoundException.class,
    //         () -> invitationService.sendInvitation(command)
    //     );
    //     assertEquals("Admin not found with id: 999", exception.getMessage());
    // }

    // @Test
    // @TestTransaction
    // void sendInvitation_WithNonExistentUserEmail_ShouldThrowNotFoundException() {
    //     // Arrange
    //     SendInvitationCommand command = new SendInvitationCommand(
    //         group.getId(),
    //         "nonexistent@example.com",
    //         admin.getId()
    //     );

    //     // Act & Assert
    //     NotFoundException exception = assertThrows(
    //         NotFoundException.class,
    //         () -> invitationService.sendInvitation(command)
    //     );
    //     assertEquals("Invitee not found with email: nonexistent@example.com", exception.getMessage());
    // }

    // @Test
    // @TestTransaction
    // void sendInvitation_ToGroupAdmin_ShouldThrowInvalidDomainArgumentException() {
    //     // Arrange
    //     SendInvitationCommand command = new SendInvitationCommand(
    //         group.getId(),
    //         admin.getEmail().getValue(),
    //         admin.getId()
    //     );

    //     // Act & Assert
    //     assertThrows(
    //         InvalidDomainArgumentException.class,
    //         () -> invitationService.sendInvitation(command)
    //     );
    // }

    // @Test
    // @TestTransaction
    // void sendInvitation_ToExistingMember_ShouldThrowInvalidDomainArgumentException() {
    //     // Arrange
    //     SendInvitationCommand command = new SendInvitationCommand(
    //         group.getId(),
    //         alreadyMember.getEmail().getValue(),
    //         admin.getId()
    //     );

    //     // Act & Assert
    //     assertThrows(
    //         InviteeAlreadyInGroupException.class,
    //         () -> invitationService.sendInvitation(command)
    //     );
    // }

    // @Test
    // @TestTransaction
    // void sendInvitation_WithExistingInvitation_ShouldThrowInvitationAlreadyExistsException() {
    //     // Arrange
    //     SendInvitationCommand command = new SendInvitationCommand(
    //         group.getId(),
    //         alreadyInvited.getEmail().getValue(),
    //         admin.getId()
    //     );

    //     // Act & Assert
    //     InvitationAlreadyExistsException exception = assertThrows(
    //         InvitationAlreadyExistsException.class,
    //         () -> invitationService.sendInvitation(command)
    //     );
    //     assertEquals("Invitation already exists", exception.getMessage());
    // }

    @Test
    @TestTransaction
    void respondToInvitation_AcceptInvitation_Success() {
        // Arrange
        RespondToInvitationCommand command = new RespondToInvitationCommand(
            group.getId(),
            InvitationResponseOperationType.ACCEPT,
            alreadyInvited.getId()
        );

        // Act
        InvitationRepresentation result = invitationService.respondToInvitation(command);

        // Assert
        assertNotNull(result);
        assertEquals(group.getId(), result.groupId());
        assertEquals(alreadyInvited.getId(), result.inviteeId());
        assertEquals(InvitationStatus.ACCEPTED, result.status());
        assertNotNull(result.createdAt());

        // Verify the invitation was updated in the database
        InvitationId invitationId = new InvitationId(group.getId(), alreadyInvited.getId());
        Invitation savedInvitation = invitationRepository.findById(invitationId).orElseThrow();
        assertEquals(InvitationStatus.ACCEPTED, savedInvitation.getStatus());
    }

    @Test
    @TestTransaction
    void respondToInvitation_RejectInvitation_Success() {
        // Arrange
        RespondToInvitationCommand command = new RespondToInvitationCommand(
            group.getId(),
            InvitationResponseOperationType.REJECT,
            alreadyInvited.getId()
        );

        // Act
        InvitationRepresentation result = invitationService.respondToInvitation(command);

        // Assert
        assertNotNull(result);
        assertEquals(group.getId(), result.groupId());
        assertEquals(alreadyInvited.getId(), result.inviteeId());
        assertEquals(InvitationStatus.REJECTED, result.status());
        assertNotNull(result.createdAt());

        // Verify the invitation was updated in the database
        InvitationId invitationId = new InvitationId(group.getId(), alreadyInvited.getId());
        Invitation savedInvitation = invitationRepository.findById(invitationId).orElseThrow();
        assertEquals(InvitationStatus.REJECTED, savedInvitation.getStatus());
    }

    @Test
    @TestTransaction
    void respondToInvitation_UserNotFound_ThrowsNotFoundException() {
        // Arrange
        Long nonExistentUserId = 999L;
        RespondToInvitationCommand command = new RespondToInvitationCommand(
            group.getId(),
            InvitationResponseOperationType.ACCEPT,
            nonExistentUserId
        );

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> invitationService.respondToInvitation(command)
        );
        assertEquals("User not found with id: " + nonExistentUserId, exception.getMessage());
    }

    @Test
    @TestTransaction
    void respondToInvitation_GroupNotFound_ThrowsNotFoundException() {
        // Arrange
        Long nonExistentGroupId = 999L;
        RespondToInvitationCommand command = new RespondToInvitationCommand(
            nonExistentGroupId,
            InvitationResponseOperationType.ACCEPT,
            alreadyInvited.getId()
        );

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> invitationService.respondToInvitation(command)
        );
        assertEquals("Group not found with id: " + nonExistentGroupId, exception.getMessage());
    }

    @Test
    @TestTransaction
    void respondToInvitation_DifferentUserResponding_ThrowsNotFoundException() {
        // Arrange
        RespondToInvitationCommand command = new RespondToInvitationCommand(
            group.getId(),
            InvitationResponseOperationType.ACCEPT,
            newInvitee.getId()  // User exists but does not have pending invitation
        );

        // Act & Assert
        NotFoundException notFoundException = assertThrows(
            NotFoundException.class,
            () -> invitationService.respondToInvitation(command)
        );
        assertEquals("Invitation not found", notFoundException.getMessage());
    }
    
    @Test
    @TestTransaction
    void getInvitations_WithStatusFilter_ReturnsFilteredInvitations() {
        // Arrange
        // Test with alreadyInvited user who has a PENDING invitation
        
        // Act
        InvitationsRepresentation result = invitationService.getInvitations(
            alreadyInvited.getId(), 
            InvitationStatus.PENDING
        );
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.invitations());
        assertEquals(1, result.invitations().size());
        
        InvitationRepresentation invitation = result.invitations().get(0);
        assertEquals(group.getId(), invitation.groupId());
        assertEquals(alreadyInvited.getId(), invitation.inviteeId());
        assertEquals(InvitationStatus.PENDING, invitation.status());
    }
    
    // @Test
    // @TestTransaction
    // void getInvitations_WithoutStatusFilter_ReturnsAllInvitations() {
    //     RespondToInvitationCommand acceptCommand = new RespondToInvitationCommand(
    //         group.getId(),
    //         InvitationResponseOperationType.ACCEPT,
    //         alreadyInvited.getId()
    //     );
    //     invitationService.respondToInvitation(acceptCommand);
        
    //     SendInvitationCommand sendCommand = new SendInvitationCommand(
    //         group.getId(),
    //         newInvitee.getEmail().getValue(),
    //         admin.getId()
    //     );
    //     invitationService.sendInvitation(sendCommand);
        
    //     // Act
    //     InvitationsRepresentation result = invitationService.getInvitations(
    //         alreadyInvited.getId(), 
    //         null
    //     );
        
    //     // Assert
    //     assertNotNull(result);
    //     assertNotNull(result.invitations());
        
    //     assertTrue(result.invitations().size() >= 1);
        
    //     // Check that we have at least one ACCEPTED invitation
    //     boolean hasAccepted = false;
        
    //     for (InvitationRepresentation invitation : result.invitations()) {
    //         if (invitation.status() == InvitationStatus.ACCEPTED) {
    //             hasAccepted = true;
    //             break;
    //         }
    //     }
        
    //     assertTrue(hasAccepted, "Should have an ACCEPTED invitation");
    // }
    
    @Test
    @TestTransaction
    void getInvitations_NoInvitations_ReturnsEmptyList() {
        // Act
        InvitationsRepresentation result = invitationService.getInvitations(
            newInvitee.getId(), // User with no invitations
            InvitationStatus.PENDING
        );
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.invitations());
        assertEquals(0, result.invitations().size());
    }
    
    @Test
    @TestTransaction
    void getInvitations_NonExistentUser_ThrowsNotFoundException() {
        // Act & Assert
        assertThrows(
            NotFoundException.class,
            () -> invitationService.getInvitations(999L, null)
        );
    }
}