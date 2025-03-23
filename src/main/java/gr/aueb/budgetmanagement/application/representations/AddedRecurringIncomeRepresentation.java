package gr.aueb.budgetmanagement.application.representations;

import com.fasterxml.jackson.annotation.JsonProperty;
import gr.aueb.budgetmanagement.domain.enums.IncomeCategory;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AddedRecurringIncomeRepresentation(
    Long id,
    String name,
    BigDecimal amount,
    IncomeCategory category,
    @JsonProperty("start_date")
    LocalDate startDate,
    @JsonProperty("end_date")
    LocalDate endDate,
    @JsonProperty("last_applied_date")
    LocalDate lastAppliedDate,
    @JsonProperty("is_stopped")
    boolean isStopped
) {}
