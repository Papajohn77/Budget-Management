package gr.aueb.budgetmanagement.infrastructure.persistence.repositories;

import java.util.List;
import java.util.Optional;

import gr.aueb.budgetmanagement.application.repositories.InvitationRepository;
import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.Invitation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;
import gr.aueb.budgetmanagement.domain.valueobjects.InvitationId;
import jakarta.persistence.EntityManager;

public class JpaInvitationRepository implements InvitationRepository {
    private final EntityManager em;

    public JpaInvitationRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(Invitation invitation) {
        em.persist(invitation);
    }

    @Override
    public Optional<Invitation> findById(InvitationId id) {
        return Optional.ofNullable(em.find(Invitation.class, id));
    }

    @Override
    public List<Invitation> findByInvitee(User invitee) {
        return em.createQuery(
            "SELECT i FROM Invitation i WHERE i.invitee = :invitee", 
            Invitation.class)
            .setParameter("invitee", invitee)
            .getResultList();
    }

    @Override
    public List<Invitation> findByAdmin(User admin) {
        return em.createQuery(
            "SELECT i FROM Invitation i WHERE i.group.admin = :admin", 
            Invitation.class)
            .setParameter("admin", admin)
            .getResultList();
    }

    @Override
    public List<Invitation> findByGroup(Group group) {
        return em.createQuery(
            "SELECT i FROM Invitation i WHERE i.group = :group", 
            Invitation.class)
            .setParameter("group", group)
            .getResultList();
    }

    @Override
    public void delete(Invitation invitation) {
        em.remove(invitation);
    }

    @Override
    public List<Invitation> findByInviteeAndStatus(User invitee, InvitationStatus status) {
        return em.createQuery(
            "SELECT i FROM Invitation i WHERE i.invitee = :invitee AND i.status = :status",
            Invitation.class)
            .setParameter("invitee", invitee)
            .setParameter("status", status)
            .getResultList();
    }
}
