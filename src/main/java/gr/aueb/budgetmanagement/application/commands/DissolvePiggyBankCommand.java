package gr.aueb.budgetmanagement.application.commands;

import jakarta.validation.constraints.NotNull;

public record DissolvePiggyBankCommand(
    @NotNull(message = "Piggy bank ID cannot be null")
    Long piggyBankId,
    
    @NotNull(message = "User ID cannot be null")
    Long userId
) {}
