package gr.aueb.budgetmanagement.domain.entities;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.exceptions.InviteeAlreadyInGroupException;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;

class InvitationTest {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String INVITEE_USERNAME = "invitee";
    private static final String INVITEE_EMAIL = "invitee@example.com";
    private static final String TEST_PASSWORD = "Test123!@#";
    private static final String GROUP_NAME = "Test Group";
    
    private User admin;
    private User invitee;
    private Group group;
    private Invitation invitation;

    @BeforeEach
    void setUp() throws Exception {
        try {
            admin = User.create(
                ADMIN_USERNAME,
                ADMIN_EMAIL,
                TEST_PASSWORD,
                new BCryptPasswordEncoder()
            );
            invitee = User.create(
                INVITEE_USERNAME,
                INVITEE_EMAIL,
                TEST_PASSWORD,
                new BCryptPasswordEncoder()
            );
            group = Group.create(GROUP_NAME, admin);

            // Set IDs using reflection since we're in a test environment
            setId(admin, 1L);
            setId(invitee, 2L);
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
        // Note: Assuming InvitationId has appropriate equals method
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
    void accept_WhenPending_ShouldChangeStatusAndAddMember() {
        invitation.accept();
        
        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
        assertTrue(group.getMembers().contains(invitee));
    }

    @Test
    void accept_WhenAlreadyAccepted_ShouldThrowException() {
        invitation.accept();
        
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> invitation.accept()
        );
    }

    @Test
    void accept_WhenRejected_ShouldThrowException() {
        invitation.reject();
        
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> invitation.accept()
        );
    }

    @Test
    void reject_WhenPending_ShouldChangeStatus() {
        invitation.reject();
        
        assertEquals(InvitationStatus.REJECTED, invitation.getStatus());
        assertFalse(group.getMembers().contains(invitee));
    }

    @Test
    void reject_WhenAlreadyRejected_ShouldThrowException() {
        invitation.reject();
        
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> invitation.reject()
        );
    }

    @Test
    void reject_WhenAccepted_ShouldThrowException() {
        invitation.accept();
        
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> invitation.reject()
        );
    }

    @Test
    void accept_ThenReject_ShouldThrowException() {
        invitation.accept();
        
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> invitation.reject()
        );
    }

    @Test
    void reject_ThenAccept_ShouldThrowException() {
        invitation.reject();
        
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> invitation.accept()
        );
    }
}