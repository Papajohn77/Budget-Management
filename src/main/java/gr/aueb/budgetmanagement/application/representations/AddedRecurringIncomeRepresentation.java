package gr.aueb.budgetmanagement.application.representations;

import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import java.time.LocalDate;

public record AddedRecurringIncomeRepresentation(
        Long id,
        String name,
        Money amount,
        IncomeCategory category,
        LocalDate startDate,
        LocalDate endDate,
        LocalDate lastAppliedDate,
        boolean isStopped
) {}