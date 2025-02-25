package gr.aueb.budgetmanagement.application.dto;

import java.time.LocalDateTime;

import gr.aueb.budgetmanagement.domain.enums.InvitationStatus;
import gr.aueb.budgetmanagement.domain.valueobjects.EmailAddress;

public record InvitationDTO(
    Long groupId,
    EmailAddress adminEmail,
    EmailAddress inviteeEmail,
    InvitationStatus status,
    LocalDateTime createdAt
) {}
