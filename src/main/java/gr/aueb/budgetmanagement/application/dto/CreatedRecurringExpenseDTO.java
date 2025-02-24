package gr.aueb.budgetmanagement.application.dto;

import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import java.time.LocalDate;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;

public record CreatedRecurringExpenseDTO(
        Long id,
        String name,
        Money amount,
        ExpenseCategory category,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate lastAppliedDate,
        boolean isStopped
) {}