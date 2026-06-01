package gr.aueb.budgetmanagement.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.InvitationResponseOperationType;
import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;
import gr.aueb.budgetmanagement.domain.exceptions.ForbiddenOperationDomainException;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.exceptions.InvitationAlreadyRespondedToException;
import gr.aueb.budgetmanagement.domain.exceptions.InviteeAlreadyInGroupException;

class InvitationTest {
    private static final String GROUP_NAME = "Test Group";
    
    private User admin;
    private User invitee;
    private Group group;
    private Invitation invitation;

    @BeforeEach
    void setUp() throws Exception {
        try {
            admin = User.create(1L);
            invitee = User.create(2L);
            group = Group.create(GROUP_NAME, admin);

            // Set IDs using reflection since we're in a test environment
            setId(group, 3L);

            invitation = Invitation.create(group, invitee, admin);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // Helper method to set ID using reflection
    private void setId(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    @Test
    void create_WithValidData_ShouldSucceed() {
        assertNotNull(invitation);
        assertEquals(InvitationStatus.PENDING, invitation.getStatus());
        assertEquals(group, invitation.getGroup());
        assertEquals(invitee, invitation.getInvitee());
        assertEquals(admin, invitation.getAdmin());
        assertNotNull(invitation.getCreatedAt());
        assertTrue(invitee.getInvitations().contains(invitation));
    }

    @Test
    void create_WithNullGroup_ShouldThrowException() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> Invitation.create(null, invitee, admin)
        );
    }

    @Test
    void create_WithNullInvitee_ShouldThrowException() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> Invitation.create(group, null, admin)
        );
    }

    @Test
    void create_WithInviteeAsAdmin_ShouldThrowException() {
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> Invitation.create(group, admin, admin)
        );
    }

    @Test
    void create_WithInviteeAlreadyInGroup_ShouldThrowException() {
        group.addMember(invitee);
        assertThrows(
            InviteeAlreadyInGroupException.class,
            () -> Invitation.create(group, invitee, admin)
        );
    }

    @Test
    void getId_ShouldReturnCorrectId() {
        assertNotNull(invitation.getId());
    }

    @Test
    void getGroup_ShouldReturnCorrectGroup() {
        assertEquals(group, invitation.getGroup());
    }

    @Test
    void getInvitee_ShouldReturnCorrectInvitee() {
        assertEquals(invitee, invitation.getInvitee());
    }

    @Test
    void getAdmin_ShouldReturnGroupAdmin() {
        assertEquals(admin, invitation.getAdmin());
    }

    @Test
    void getStatus_InitialStatus_ShouldBePending() {
        assertEquals(InvitationStatus.PENDING, invitation.getStatus());
    }

    @Test
    void getCreatedAt_ShouldNotBeNull() {
        assertNotNull(invitation.getCreatedAt());
    }

    @Test
    void accept_WhenNotInvitee_shouldThrowException() {
        assertThrows(
            ForbiddenOperationDomainException.class, 
            () -> invitation.respond(InvitationResponseOperationType.ACCEPT, admin)
        );
    }

    @Test
    void accept_WhenPending_ShouldChangeStatusAndAddMember() {
        invitation.respond(InvitationResponseOperationType.ACCEPT, invitee);
        
        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
        assertTrue(group.getMembers().contains(invitee));
    }

    @Test
    void accept_WhenAlreadyAccepted_ShouldThrowException() {
        invitation.respond(InvitationResponseOperationType.ACCEPT, invitee);
        
        assertThrows(
            InvitationAlreadyRespondedToException.class,
            () -> invitation.respond(InvitationResponseOperationType.ACCEPT, invitee)
        );
    }

    @Test
    void accept_WhenRejected_ShouldThrowException() {
        invitation.respond(InvitationResponseOperationType.REJECT, invitee);
        
        assertThrows(
            InvitationAlreadyRespondedToException.class,
            () -> invitation.respond(InvitationResponseOperationType.ACCEPT, invitee)
        );
    }

    @Test
    void reject_WhenPending_ShouldChangeStatus() {
        invitation.respond(InvitationResponseOperationType.REJECT, invitee);
        
        assertEquals(InvitationStatus.REJECTED, invitation.getStatus());
        assertFalse(group.getMembers().contains(invitee));
    }

    @Test
    void reject_WhenAlreadyRejected_ShouldThrowException() {
        invitation.respond(InvitationResponseOperationType.REJECT, invitee);
        
        assertThrows(
            InvitationAlreadyRespondedToException.class,
            () -> invitation.respond(InvitationResponseOperationType.REJECT, invitee)
        );
    }

    @Test
    void reject_WhenAccepted_ShouldThrowException() {
        invitation.respond(InvitationResponseOperationType.ACCEPT, invitee);
        
        assertThrows(
            InvitationAlreadyRespondedToException.class,
            () -> invitation.respond(InvitationResponseOperationType.REJECT, invitee)
        );
    }

    @Test
    void equals_WithSameInstance_ShouldBeEqual() {
        assertEquals(invitation, invitation);
    }

    @Test
    void equals_WithNull_ShouldNotBeEqual() {
        assertNotEquals(null, invitation);
    }

    @Test
    void equals_WithDifferentType_ShouldNotBeEqual() {
        // Arrange
        Object other = new Object();

        // Assert
        assertNotEquals(invitation, other);
    }

    @Test
    void equalsAndHashCodeWithDifferentGroup() throws Exception {
        // Arrange
        Invitation other = createInvitationForDifferentGroup();

        // Assert
        assertNotEquals(invitation, other);
    }

    private Invitation createInvitationForDifferentGroup() throws Exception {
        try {
            admin = User.create(1L);
            invitee = User.create(2L);
            group = Group.create(GROUP_NAME, admin);

            // Set IDs using reflection since we're in a test environment
            setId(group, 4L);

            return Invitation.create(group, invitee, admin);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
