package gr.aueb.budgetmanagement.application.representations;

import java.time.LocalDateTime;

import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;

public record InvitationRepresentation(
    Long groupId,
    Long inviteeId,
    InvitationStatus status,
    LocalDateTime createdAt
) {}
