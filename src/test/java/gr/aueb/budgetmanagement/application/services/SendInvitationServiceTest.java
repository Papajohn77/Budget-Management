package gr.aueb.budgetmanagement.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.application.commands.SendInvitationCommand;
import gr.aueb.budgetmanagement.application.dto.InvitationDTO;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.Invitation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.exceptions.InvitationAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.ports.PasswordHasher;
import gr.aueb.budgetmanagement.domain.valueobjects.InvitationId;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaGroupRepository;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaInvitationRepository;
import gr.aueb.budgetmanagement.infrastructure.persistence.repositories.JpaUserRepository;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class SendInvitationServiceTest {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_EMAIL = "admin@example.com";
    private static final String INVITEE_USERNAME = "invitee";
    private static final String INVITEE_EMAIL = "invitee@example.com";
    private static final String TEST_PASSWORD = "Test123!@#";
    private static final String GROUP_NAME = "Test Group";

    private EntityManager entityManager;
    private EntityTransaction transaction;
    private JpaUserRepository userRepository;
    private JpaGroupRepository groupRepository;
    private JpaInvitationRepository invitationRepository;
    private SendInvitationService sendInvitationService;
    private PasswordHasher passwordHasher;
    private User admin;
    private User invitee;
    private Group group;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        userRepository = new JpaUserRepository(entityManager);
        groupRepository = new JpaGroupRepository(entityManager);
        invitationRepository = new JpaInvitationRepository(entityManager);
        sendInvitationService = new SendInvitationService(invitationRepository, userRepository, groupRepository);
        passwordHasher = new BCryptPasswordEncoder();

        createTestUsers();
        createTestGroup();
    }

    @AfterEach
    void tearDown() {
        if (transaction.isActive()) {
            transaction.rollback();
        }
        entityManager.close();
    }

    private void createTestUsers() {
        admin = User.create(
            ADMIN_USERNAME,
            ADMIN_EMAIL,
            TEST_PASSWORD,
            passwordHasher);
        entityManager.persist(admin);

        invitee = User.create(
            INVITEE_USERNAME,
            INVITEE_EMAIL,
            TEST_PASSWORD,
            passwordHasher);
        entityManager.persist(invitee);

        // Flush to ensure IDs are assigned
        entityManager.flush();
    }

    private void createTestGroup() {
        group = Group.create(GROUP_NAME, admin);
        entityManager.persist(group);
        entityManager.flush();
    }

    @Test
    void sendInvitation_WithValidData_ShouldCreateInvitation() {
        // Arrange
        SendInvitationCommand command = new SendInvitationCommand(
            group.getId(), 
            INVITEE_EMAIL, 
            admin.getId()
        );

        // Act
        InvitationDTO result = sendInvitationService.sendInvitation(command);

        // Assert
        assertNotNull(result);
        assertEquals(group.getId(), result.groupId());
        assertEquals(ADMIN_EMAIL, result.adminEmail().getValue());
        assertEquals(INVITEE_EMAIL, result.inviteeEmail().getValue());
        assertEquals(InvitationStatus.PENDING, result.status());
        assertNotNull(result.createdAt());

        // Verify the invitation was persisted
        InvitationId invitationId = new InvitationId(group.getId(), invitee.getId());
        Invitation savedInvitation = entityManager.find(Invitation.class, invitationId);
        assertNotNull(savedInvitation);
        assertEquals(InvitationStatus.PENDING, savedInvitation.getStatus());
        assertEquals(group.getId(), savedInvitation.getGroup().getId());
        assertEquals(invitee.getId(), savedInvitation.getInvitee().getId());
        
        // Verify the invitee has the invitation in their collection
        assertTrue(invitee.getInvitations().contains(savedInvitation));
    }

    @Test
    void sendInvitation_WithNonExistentGroup_ShouldThrowNotFoundException() {
        // Arrange
        SendInvitationCommand command = new SendInvitationCommand(
            999L, 
            INVITEE_EMAIL, 
            admin.getId()
        );

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> sendInvitationService.sendInvitation(command)
        );
        assertEquals("Group not found with id: 999", exception.getMessage());
    }

    @Test
    void sendInvitation_WithNonExistentUser_ShouldThrowNotFoundException() {
        // Arrange
        SendInvitationCommand command = new SendInvitationCommand(
            group.getId(), 
            "nonexistent@example.com", 
            admin.getId()
        );

        // Act & Assert
        NotFoundException exception = assertThrows(
            NotFoundException.class,
            () -> sendInvitationService.sendInvitation(command)
        );
        assertEquals("Invitee not found with email: nonexistent@example.com", exception.getMessage());
    }

    @Test
    void sendInvitation_ToGroupAdmin_ShouldThrowInvalidDomainArgumentException() {
        // Arrange
        SendInvitationCommand command = new SendInvitationCommand(
            group.getId(), 
            ADMIN_EMAIL, 
            admin.getId()
        );

        // Act & Assert
        assertThrows(
            InvalidDomainArgumentException.class,
            () -> sendInvitationService.sendInvitation(command)
        );
    }

    @Test
    void sendInvitation_ToExistingMember_ShouldThrowInvalidDomainArgumentException() {
        // Arrange
        group.addMember(invitee);
        entityManager.flush();
        
        SendInvitationCommand command = new SendInvitationCommand(
            group.getId(), 
            INVITEE_EMAIL, 
            admin.getId());

        // Act & Assert
        assertThrows(
            gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException.class,
            () -> sendInvitationService.sendInvitation(command)
        );
    }

    @Test
    void sendInvitation_WithExistingInvitation_ShouldThrowInvitationAlreadyExistsException() {
        // Arrange
        SendInvitationCommand command = new SendInvitationCommand(
            group.getId(), 
            INVITEE_EMAIL, 
            admin.getId()
        );

        // First, send an invitation
        sendInvitationService.sendInvitation(command);
        
        // Act: Try to send another invitation to the same invitee for the same group
        InvitationAlreadyExistsException exception = assertThrows(
            InvitationAlreadyExistsException.class,
            () -> sendInvitationService.sendInvitation(command)
        );

        // Assert: Ensure the correct exception is thrown
        assertEquals("Invitation already exists", exception.getMessage());

        // Verify that the first invitation still exists in the system
        InvitationId invitationId = new InvitationId(group.getId(), invitee.getId());
        Invitation invitation = entityManager.find(Invitation.class, invitationId);
        assertNotNull(invitation);
        assertEquals(InvitationStatus.PENDING, invitation.getStatus());
    }
}
