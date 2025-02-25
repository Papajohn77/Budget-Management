package gr.aueb.budgetmanagement.application.commands;

import gr.aueb.budgetmanagement.domain.enums.InvitationResponseOperationType;
import jakarta.validation.constraints.NotNull;

public record RespondToInvitationCommand(
    @NotNull(message = "Group ID cannot be null")
    Long groupId,
    
    @NotNull(message = "Invitee ID cannot be null")
    Long inviteeId,

    @NotNull(message = "Responder ID cannot be null")
    Long responderId,
    
    @NotNull(message = "Response type cannot be null")
    InvitationResponseOperationType response
) {}