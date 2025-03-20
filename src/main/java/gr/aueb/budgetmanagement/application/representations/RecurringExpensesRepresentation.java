package gr.aueb.budgetmanagement.application.representations;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RecurringExpensesRepresentation(
    @JsonProperty("recurring_expenses")
    List<AddedRecurringExpenseRepresentation> recurringExpenses
) {}
