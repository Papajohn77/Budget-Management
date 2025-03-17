package gr.aueb.budgetmanagement.presentation.api.requests;

import gr.aueb.budgetmanagement.domain.enums.InvitationResponseOperationType;

public record UpdateInvitationStatusRequest(
    InvitationResponseOperationType status
) {}
