package gr.aueb.budgetmanagement.domain.entities;

import java.time.LocalDateTime;

import gr.aueb.budgetmanagement.domain.enums.InvitationResponseOperationType;
import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.domain.exceptions.InvitationAlreadyExistsException;
import gr.aueb.budgetmanagement.domain.exceptions.UnauthorizedOperationException;
import gr.aueb.budgetmanagement.domain.valueobjects.InvitationId;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "invitations")
public class Invitation {
    @EmbeddedId
    private InvitationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("groupId")
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("inviteeId")
    @JoinColumn(name = "invitee_id", nullable = false)
    private User invitee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Invitation() {}

    public static Invitation create(Group group, User invitee, User admin) {
        if (group == null) {
            throw new InvalidDomainArgumentException("Group cannot be null");
        }

        if (invitee == null) {
            throw new InvalidDomainArgumentException("Invitee cannot be null");
        }

        if (invitee.hasAlreadyBeenInvitedTo(group)) {
            throw new InvitationAlreadyExistsException("Invitation already exists");
        }

        if (admin != group.getAdmin()) {
            throw new UnauthorizedOperationException("Only the group admin can send invitations");
        }

        if (group.getAdmin().equals(invitee)) {
            throw new InvalidDomainArgumentException("Cannot invite group admin");
        }

        if (group.getMembers().contains(invitee)) {
            throw new InvalidDomainArgumentException("User is already a member of the group");
        }

        Invitation invitation = new Invitation();
        invitation.id = new InvitationId(group.getId(), invitee.getId());
        invitation.group = group;
        invitation.invitee = invitee;
        invitation.status = InvitationStatus.PENDING;
        invitation.createdAt = LocalDateTime.now();
        invitee.addInvitation(invitation);
        return invitation;
    }

    public InvitationId getId() {
        return id;
    }

    public Group getGroup() {
        return group;
    }

    public User getInvitee() {
        return invitee;
    }

    public User getAdmin() {
        return group.getAdmin();
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    void accept() {
        if (status != InvitationStatus.PENDING) {
            throw new InvalidDomainArgumentException("Can only accept pending invitations");
        }
        status = InvitationStatus.ACCEPTED;
        group.addMember(invitee);
    }

    void reject() {
        if (status != InvitationStatus.PENDING) {
            throw new InvalidDomainArgumentException("Can only decline pending invitations");
        }
        status = InvitationStatus.REJECTED;
    }

    public Invitation respond(InvitationResponseOperationType operationType, User user) {
        if (!user.containsInvitation(this)) {
            throw new UnauthorizedOperationException("User cannot respond to invitation");
        }
        switch (operationType) {
            case ACCEPT -> accept();
            case REJECT -> reject();
        }
        return this;
    }
}
