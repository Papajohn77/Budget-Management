package gr.aueb.budgetmanagement.application.commands;

import java.time.LocalDate;

import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.validation.constraints.NotNull;

public record AllocateToPiggyBankCommand(
    @NotNull(message = "Date cannot be null")
    LocalDate date,
    
    @NotNull(message = "Amount cannot be null")
    Money amount,
    
    @NotNull(message = "Piggy bank ID cannot be null")
    Long piggyBankId,
    
    @NotNull(message = "User ID cannot be null")
    Long userId
) {}
