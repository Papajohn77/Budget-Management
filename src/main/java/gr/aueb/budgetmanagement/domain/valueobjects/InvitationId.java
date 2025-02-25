package gr.aueb.budgetmanagement.domain.valueobjects;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class InvitationId implements Serializable {
    private Long groupId;
    private Long inviteeId;

    protected InvitationId() {}

    public InvitationId(Long groupId, Long inviteeId) {
        if (groupId == null || inviteeId == null) {
            throw new IllegalArgumentException("groupId and inviteeId must not be null");
        }
        this.groupId = groupId;
        this.inviteeId = inviteeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InvitationId)) return false;
        InvitationId that = (InvitationId) o;
        return Objects.equals(groupId, that.groupId) &&
               Objects.equals(inviteeId, that.inviteeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, inviteeId);
    }
}