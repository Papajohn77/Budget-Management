package gr.aueb.budgetmanagement.domain.valueobjects;

import java.io.Serializable;
import java.util.Objects;

import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import jakarta.persistence.Embeddable;

@Embeddable
public class InvitationId implements Serializable {
    private Long groupId;
    private Long inviteeId;

    protected InvitationId() {

    }

    public InvitationId(Long groupId, Long inviteeId) {
        if (groupId == null) {
            throw new InvalidDomainArgumentException("Group ID cannot be null");
        }

        if (inviteeId == null) {
            throw new InvalidDomainArgumentException("Invitee ID cannot be null");
        }

        this.groupId = groupId;
        this.inviteeId = inviteeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InvitationId invitationId = (InvitationId) o;
        return Objects.equals(groupId, invitationId.groupId) &&
            Objects.equals(inviteeId, invitationId.inviteeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, inviteeId);
    }
}