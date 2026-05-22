package gr.aueb.budgetmanagement.application.representations;

import java.util.List;

public record InvitationsRepresentation(
    List<InvitationRepresentation> invitations
) {}