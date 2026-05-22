package gr.aueb.budgetmanagement.presentation.api.requests;

public record SendInvitationRequest(
    Long groupId,
    String email
) {}