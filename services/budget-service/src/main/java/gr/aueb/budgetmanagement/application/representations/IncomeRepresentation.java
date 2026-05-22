package gr.aueb.budgetmanagement.application.representations;

import java.math.BigDecimal;
import java.time.LocalDate;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;

public record IncomeRepresentation(
    Long id,
    BigDecimal amount,
    LocalDate date,
    IncomeCategory category
) {}