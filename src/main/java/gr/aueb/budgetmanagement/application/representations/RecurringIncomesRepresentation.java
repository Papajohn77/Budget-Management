package gr.aueb.budgetmanagement.application.representations;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record RecurringIncomesRepresentation(
    @JsonProperty("recurring_incomes")
    List<AddedRecurringIncomeRepresentation> recurringIncomes
) {}
