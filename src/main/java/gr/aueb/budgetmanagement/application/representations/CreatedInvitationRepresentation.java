package gr.aueb.budgetmanagement.application.representations;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CreatedInvitationRepresentation(
    @JsonProperty("group_id")
    Long groupId,
    
    @JsonProperty("invitee_id")
    Long inviteeId,
    
    String status
) {}