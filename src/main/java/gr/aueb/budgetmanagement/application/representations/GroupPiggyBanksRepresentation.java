package gr.aueb.budgetmanagement.application.representations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GroupPiggyBanksRepresentation(
    String name,
    Long groupId,
    @JsonProperty("piggy_banks")
    List<CreatedGroupPiggyBankRepresentation> piggyBanks
) {}