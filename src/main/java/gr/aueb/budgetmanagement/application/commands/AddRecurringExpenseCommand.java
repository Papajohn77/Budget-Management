package gr.aueb.budgetmanagement.application.commands;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddRecurringExpenseCommand(
    @NotBlank(message = "Name cannot be blank")
    String name,

    @NotNull(message = "Amount cannot be null")
    Money amount,

    @NotNull(message = "Category cannot be null")
    ExpenseCategory category,

    @NotNull(message = "Start date cannot be null")
    LocalDate startDate,

    @NotNull(message = "End date cannot be null")
    LocalDate endDate,

    @NotNull(message = "User ID cannot be null") 
    Long userId
) {}
