package gr.aueb.budgetmanagement.application.representations;

import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import java.time.LocalDate;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;

public record AddedIncomeRepresentation(
        Long id,
        Money amount,
        LocalDate date,
        IncomeCategory category
)
{}