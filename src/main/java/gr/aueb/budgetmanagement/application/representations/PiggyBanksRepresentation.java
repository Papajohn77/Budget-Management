package gr.aueb.budgetmanagement.application.representations;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PiggyBanksRepresentation(
    @JsonProperty("personal_piggy_banks")
    List<CreatedPersonalPiggyBankRepresentation> personalPiggyBanks,
    
    @JsonProperty("group_piggy_banks")
    List<GroupPiggyBanksRepresentation> groupPiggyBanks
) {}