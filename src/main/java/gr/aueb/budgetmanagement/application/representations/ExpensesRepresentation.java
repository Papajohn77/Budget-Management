package gr.aueb.budgetmanagement.application.representations;

import java.util.List;

public record ExpensesRepresentation(
    List<AddedExpenseRepresentation> expenses
) {}