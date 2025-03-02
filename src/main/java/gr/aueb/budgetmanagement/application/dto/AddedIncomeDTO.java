package gr.aueb.budgetmanagement.application.dto;

import gr.aueb.budgetmanagement.domain.valueobjects.Money;
import java.time.LocalDate;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;

public record AddedIncomeDTO(
        Long id,
        Money amount,
        LocalDate date,
        IncomeCategory category
)
{}