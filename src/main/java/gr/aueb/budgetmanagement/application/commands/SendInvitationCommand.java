package gr.aueb.budgetmanagement.application.commands;

import gr.aueb.budgetmanagement.domain.enums.InvitationOperationType;
import jakarta.validation.constraints.NotNull;

public record SendInvitationCommand(
    @NotNull(message = "Group ID cannot be null")
    Long groupId,
    
    @NotNull(message = "Email cannot be null")
    String email,
    
    @NotNull(message = "Operation type cannot be null")
    InvitationOperationType operation
) {}