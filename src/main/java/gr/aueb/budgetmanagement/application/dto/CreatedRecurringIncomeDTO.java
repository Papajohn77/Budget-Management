package gr.aueb.budgetmanagement.application.dto;

import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import java.time.LocalDate;

public record CreatedRecurringIncomeDTO(
        Long id,
        String name,
        Money amount,
        IncomeCategory category,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate lastAppliedDate,
        boolean isStopped
) {}