package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.Invitation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;
import gr.aueb.budgetmanagement.domain.valueobjects.InvitationId;
import gr.aueb.budgetmanagement.infrastructure.persistence.JPAUtil;
import gr.aueb.budgetmanagement.infrastructure.security.BCryptPasswordEncoder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

class JpaInvitationRepositoryTest {
    private static final String TEST_PASSWORD = "Test123!@#";
    
    private EntityManager entityManager;
    private EntityTransaction transaction;
    private JpaInvitationRepository repository;
    
    private User admin;
    private User invitee;
    private Group group;
    private Invitation invitation;

    @BeforeEach
    void setUp() {
        entityManager = JPAUtil.getCurrentEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();
        
        repository = new JpaInvitationRepository(entityManager);
        
        // Create and persist test data
        admin = User.create("admin", "admin@example.com", TEST_PASSWORD, new BCryptPasswordEncoder());
        invitee = User.create("invitee", "invitee@example.com", TEST_PASSWORD, new BCryptPasswordEncoder());
        group = Group.create("Test Group", admin);
        
        entityManager.persist(admin);
        entityManager.persist(invitee);
        entityManager.persist(group);
        
        invitation = Invitation.create(group, invitee);
    }

    @Test
    void save_WithValidInvitation_ShouldPersist() {
        // Act
        repository.save(invitation);

        // Assert
        Invitation found = entityManager.find(Invitation.class, invitation.getId());
        assertNotNull(found);
        assertEquals(invitation.getGroup(), found.getGroup());
        assertEquals(invitation.getInvitee(), found.getInvitee());
        assertEquals(invitation.getStatus(), found.getStatus());
    }

    @Test
    void findById_WithExistingInvitation_ShouldReturnInvitation() {
        // Arrange
        repository.save(invitation);

        // Act
        Optional<Invitation> found = repository.findById(invitation.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(invitation.getGroup(), found.get().getGroup());
        assertEquals(invitation.getInvitee(), found.get().getInvitee());
    }

    @Test
    void findById_WithNonExistingInvitation_ShouldReturnEmpty() {
        // Arrange
        InvitationId nonExistingId = new InvitationId(999L, 999L);

        // Act
        Optional<Invitation> found = repository.findById(nonExistingId);

        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    void findByInvitee_WithExistingInvitations_ShouldReturnList() {
        // Arrange
        repository.save(invitation);
        Group anotherGroup = Group.create("Another Group", admin);
        entityManager.persist(anotherGroup);
        Invitation anotherInvitation = Invitation.create(anotherGroup, invitee);
        repository.save(anotherInvitation);

        // Act
        List<Invitation> invitations = repository.findByInvitee(invitee);

        // Assert
        assertEquals(2, invitations.size());
        assertTrue(invitations.stream().allMatch(inv -> inv.getInvitee().equals(invitee)));
    }

    @Test
    void findByAdmin_WithExistingInvitations_ShouldReturnList() {
        // Arrange
        repository.save(invitation);
        User anotherInvitee = User.create("another", "another@example.com", TEST_PASSWORD, new BCryptPasswordEncoder());
        entityManager.persist(anotherInvitee);
        Invitation anotherInvitation = Invitation.create(group, anotherInvitee);
        repository.save(anotherInvitation);

        // Act
        List<Invitation> invitations = repository.findByAdmin(admin);

        // Assert
        assertEquals(2, invitations.size());
        assertTrue(invitations.stream().allMatch(inv -> inv.getAdmin().equals(admin)));
    }

    @Test
    void findByGroup_WithExistingInvitations_ShouldReturnList() {
        // Arrange
        repository.save(invitation);
        User anotherInvitee = User.create("another", "another@example.com", TEST_PASSWORD, new BCryptPasswordEncoder());
        entityManager.persist(anotherInvitee);
        Invitation anotherInvitation = Invitation.create(group, anotherInvitee);
        repository.save(anotherInvitation);

        // Act
        List<Invitation> invitations = repository.findByGroup(group);

        // Assert
        assertEquals(2, invitations.size());
        assertTrue(invitations.stream().allMatch(inv -> inv.getGroup().equals(group)));
    }

    @Test
    void delete_WithExistingInvitation_ShouldRemove() {
        // Arrange
        repository.save(invitation);
        assertTrue(repository.findById(invitation.getId()).isPresent());

        // Act
        repository.delete(invitation);

        // Assert
        assertTrue(repository.findById(invitation.getId()).isEmpty());
    }

    @Test
    void findByInviteeAndStatus_WithExistingInvitations_ShouldReturnFilteredList() {
        // Arrange
        repository.save(invitation); // PENDING status
        
        Group anotherGroup = Group.create("Another Group", admin);
        entityManager.persist(anotherGroup);
        Invitation acceptedInvitation = Invitation.create(anotherGroup, invitee);
        acceptedInvitation.accept();
        repository.save(acceptedInvitation);

        // Act
        List<Invitation> pendingInvitations = repository.findByInviteeAndStatus(invitee, InvitationStatus.PENDING);
        List<Invitation> acceptedInvitations = repository.findByInviteeAndStatus(invitee, InvitationStatus.ACCEPTED);

        // Assert
        assertEquals(1, pendingInvitations.size());
        assertEquals(1, acceptedInvitations.size());
        assertEquals(InvitationStatus.PENDING, pendingInvitations.get(0).getStatus());
        assertEquals(InvitationStatus.ACCEPTED, acceptedInvitations.get(0).getStatus());
    }

    @AfterEach
    void tearDown() {
        if (transaction.isActive()) {
            transaction.rollback();
        }
        entityManager.close();
    }
}