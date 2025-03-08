package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.Fixture;
import gr.aueb.budgetmanagement.application.repositories.GroupRepository;
import gr.aueb.budgetmanagement.application.repositories.InvitationRepository;
import gr.aueb.budgetmanagement.application.repositories.UserRepository;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.Invitation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.InvitationResponseOperationType;
import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;
import gr.aueb.budgetmanagement.domain.valueobjects.InvitationId;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@QuarkusTest
class JpaInvitationRepositoryTest {

    @Inject
    private EntityManager entityManager;

    @Inject
    private UserRepository userRepository;

    @Inject
    private GroupRepository groupRepository;

    @Inject
    private InvitationRepository invitationRepository;
    
    private User admin;
    private User alreadyInvited;
    private User newInvitee;
    private Group group;
    private Invitation existingInvitation;

    @BeforeEach
    void setUp() {
        admin = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
        alreadyInvited = userRepository.findById(Fixture.Users.TESTUSER2_ID).orElseThrow();
        newInvitee = userRepository.findById(Fixture.Users.TESTUSER4_ID).orElseThrow();
        group = groupRepository.findById(Fixture.Groups.TESTGROUP_ID).orElseThrow();
    
        InvitationId invitationId = new InvitationId(Fixture.Invitations.INVITATION_GROUP_ID, Fixture.Invitations.INVITATION_INVITEE_ID);
        existingInvitation = invitationRepository.findById(invitationId).orElseThrow();
    }

    @Test
    @TestTransaction
    void save_WithValidInvitation_ShouldPersist() {
        // Arrange
        group = groupRepository.findById(Fixture.Groups.TESTGROUP_ID).orElseThrow();
        newInvitee = userRepository.findById(Fixture.Users.TESTUSER4_ID).orElseThrow();
        admin = userRepository.findById(Fixture.Users.TESTUSER_ID).orElseThrow();
        Invitation newInvitation = Invitation.create(group, newInvitee, admin);

        // Act
        invitationRepository.save(newInvitation);

        // Assert
        Invitation found = entityManager.find(Invitation.class, newInvitation.getId());
        assertNotNull(found);
        assertEquals(newInvitation.getGroup(), found.getGroup());
        assertEquals(newInvitation.getInvitee(), found.getInvitee());
        assertEquals(newInvitation.getStatus(), found.getStatus());
    }

    @Test
    @TestTransaction
    void findById_WithExistingInvitation_ShouldReturnInvitation() {
        // Act
        Optional<Invitation> found = invitationRepository.findById(existingInvitation.getId());

        // Assert
        assertTrue(found.isPresent());
    }

    @Test
    @TestTransaction
    void findById_WithNonExistingInvitation_ShouldReturnEmpty() {
        // Arrange
        InvitationId nonExistingId = new InvitationId(999L, 999L);

        // Act
        Optional<Invitation> found = invitationRepository.findById(nonExistingId);

        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    @TestTransaction
    void findByInvitee_WithExistingInvitations_ShouldReturnList() {
        // Act
        List<Invitation> invitations = invitationRepository.findByInvitee(alreadyInvited);

        // Assert
        assertEquals(1, invitations.size());
    }

    @Test
    @TestTransaction
    void findByAdmin_WithExistingInvitations_ShouldReturnList() {
        // Act
        List<Invitation> invitations = invitationRepository.findByAdmin(admin);

        // Assert
        assertEquals(1, invitations.size());
    }

    @Test
    @TestTransaction
    void findByGroup_WithExistingInvitations_ShouldReturnList() {
        // Act
        List<Invitation> invitations = invitationRepository.findByGroup(group);

        // Assert
        assertEquals(1, invitations.size());
    }

    @Test
    @TestTransaction
    void delete_WithExistingInvitation_ShouldRemove() {
        // Arrange
        InvitationId invitationId = new InvitationId(Fixture.Invitations.INVITATION_GROUP_ID, Fixture.Invitations.INVITATION_INVITEE_ID);
        existingInvitation = invitationRepository.findById(invitationId).orElseThrow();

        // Act
        invitationRepository.delete(existingInvitation);

        // Assert
        assertTrue(invitationRepository.findById(existingInvitation.getId()).isEmpty());
    }

    @Test
    @TestTransaction
    void findByInviteeAndStatus_WithExistingInvitations_ShouldReturnFilteredList() {
        // Arrange
        alreadyInvited = userRepository.findById(Fixture.Users.TESTUSER2_ID).orElseThrow();

        Group anotherGroup = Group.create("Another Group", admin);
        entityManager.persist(anotherGroup);

        Invitation acceptedInvitation = Invitation.create(anotherGroup, alreadyInvited, admin);
        acceptedInvitation.respond(InvitationResponseOperationType.ACCEPT, alreadyInvited);
        invitationRepository.save(acceptedInvitation);

        // Act
        List<Invitation> pendingInvitations = invitationRepository.findByInviteeAndStatus(alreadyInvited, InvitationStatus.PENDING);
        List<Invitation> acceptedInvitations = invitationRepository.findByInviteeAndStatus(alreadyInvited, InvitationStatus.ACCEPTED);
        List<Invitation> rejectedInvitations = invitationRepository.findByInviteeAndStatus(alreadyInvited, InvitationStatus.REJECTED);

        // Assert
        assertEquals(1, pendingInvitations.size());
        assertEquals(1, acceptedInvitations.size());
        assertEquals(0, rejectedInvitations.size());

        assertEquals(InvitationStatus.PENDING, pendingInvitations.get(0).getStatus());
        assertEquals(InvitationStatus.ACCEPTED, acceptedInvitations.get(0).getStatus());
    }
}
