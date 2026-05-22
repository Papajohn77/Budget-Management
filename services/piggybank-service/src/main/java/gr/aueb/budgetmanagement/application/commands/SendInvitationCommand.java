package gr.aueb.budgetmanagement.application.commands;

import jakarta.validation.constraints.NotNull;

public record SendInvitationCommand(
    @NotNull(message = "Group ID cannot be null")
    Long groupId,
    
    @NotNull(message = "Email cannot be null")
    String email,

    @NotNull(message = "User ID cannot be null")
    Long userId
) {}