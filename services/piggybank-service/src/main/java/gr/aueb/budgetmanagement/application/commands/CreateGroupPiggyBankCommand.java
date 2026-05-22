package gr.aueb.budgetmanagement.application.commands;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateGroupPiggyBankCommand(
    @NotBlank(message = "Name cannot be blank")
    String name,
    
    @NotNull(message = "Target amount cannot be null")
    Money targetAmount,
    
    @NotNull(message = "Category cannot be null")
    ExpenseCategory category,
    
    @NotNull(message = "Group ID cannot be null")
    Long groupId,
    
    @NotNull(message = "Admin ID cannot be null")
    Long userId
) {}
