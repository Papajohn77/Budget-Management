package gr.aueb.budgetmanagement.application.representations;

import java.util.List;

public record IncomesRepresentation(
    List<AddedIncomeRepresentation> incomes
) {}