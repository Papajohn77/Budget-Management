package gr.aueb.budgetmanagement.application.commands;

import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import java.time.LocalDate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record AddExpenseCommand(
        @NotNull(message = "Amount cannot be null")
        @DecimalMin(value = "0.0", inclusive = true, message = "Amount must be positive")
        Money amount,

        @NotNull(message = "Category cannot be null")
        ExpenseCategory category,

        @NotNull(message = "Date cannot be null")
        LocalDate date,

        @NotNull(message = "User ID cannot be null")
        Long userId

)


{}
