package gr.aueb.budgetmanagement.application.representations;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;

public record InvitationRepresentation(
    @JsonProperty("group_id")
    Long groupId,

    @JsonProperty("invitee_id")
    Long inviteeId,
    
    InvitationStatus status,
    
    @JsonProperty("created_at")
    LocalDateTime createdAt
) {}
