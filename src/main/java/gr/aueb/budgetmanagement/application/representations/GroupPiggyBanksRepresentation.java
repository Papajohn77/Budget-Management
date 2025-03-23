package gr.aueb.budgetmanagement.application.representations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GroupPiggyBanksRepresentation(
    String name,
    @JsonProperty("group_id")
    Long groupId,
    @JsonProperty("piggy_banks")
    List<GroupPiggyBankRepresentation> piggyBanks
) {}