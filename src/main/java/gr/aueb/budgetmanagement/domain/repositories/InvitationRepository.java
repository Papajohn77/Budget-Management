package gr.aueb.budgetmanagement.domain.repositories;

import java.util.List;
import java.util.Optional;

import gr.aueb.budgetmanagement.domain.entities.Group;
import gr.aueb.budgetmanagement.domain.entities.Invitation;
import gr.aueb.budgetmanagement.domain.entities.User;
import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;
import gr.aueb.budgetmanagement.domain.valueobjects.InvitationId;

public interface InvitationRepository {
    void save(Invitation invitation);
    Optional<Invitation> findById(InvitationId id);
    List<Invitation> findByInvitee(User invitee);
    List<Invitation> findByInviteeAndStatus(User invitee, InvitationStatus status);
    List<Invitation> findByAdmin(User admin);
    List<Invitation> findByGroup(Group group);
    void delete(Invitation invitation);
}
