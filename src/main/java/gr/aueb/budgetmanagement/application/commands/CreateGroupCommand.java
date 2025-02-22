package gr.aueb.budgetmanagement.application.commands;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateGroupCommand(
    @NotNull(message = "User ID cannot be null")
    Long userId,
    
    @NotBlank(message = "Group name cannot be blank")
    String name
) {}
