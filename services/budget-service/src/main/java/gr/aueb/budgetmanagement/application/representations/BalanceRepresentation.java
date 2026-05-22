package gr.aueb.budgetmanagement.application.representations;

import java.math.BigDecimal;

public record BalanceRepresentation(
    BigDecimal balance
) {}
