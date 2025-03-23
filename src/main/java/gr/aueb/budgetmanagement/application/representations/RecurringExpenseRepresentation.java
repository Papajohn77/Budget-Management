package gr.aueb.budgetmanagement.application.representations;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.aueb.budgetmanagement.domain.enums.ExpenseCategory;

public record RecurringExpenseRepresentation(
    Long id,
    String name,
    BigDecimal amount,
    ExpenseCategory category,
    @JsonProperty("start_date")
    LocalDate startDate,
    @JsonProperty("end_date")
    LocalDate endDate,
    @JsonProperty("last_applied_date")
    LocalDate lastAppliedDate,
    @JsonProperty("is_stopped")
    boolean isStopped
) {}