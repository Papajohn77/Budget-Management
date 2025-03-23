package gr.aueb.budgetmanagement.application.representations;

import java.util.List;

public record GroupsRepresentation(
    List<GroupRepresentation> groups
) {}
