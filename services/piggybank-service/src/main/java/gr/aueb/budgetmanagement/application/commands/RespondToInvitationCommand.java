package gr.aueb.budgetmanagement.application.commands;

import gr.aueb.budgetmanagement.domain.enums.InvitationResponseOperationType;
import jakarta.validation.constraints.NotNull;

public record RespondToInvitationCommand(
    @NotNull(message = "Group ID cannot be null")
    Long groupId,
    
    @NotNull(message = "Response type cannot be null")
    InvitationResponseOperationType response,

    @NotNull(message = "User ID cannot be null") 
    Long userId
) {}