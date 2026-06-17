package gr.aueb.budgetmanagement.presentation.api.resources;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import gr.aueb.budgetmanagement.infrastructure.simulation.ConditionSimulator;
import gr.aueb.budgetmanagement.presentation.api.requests.SetSimulatedConditionRequest;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/simulate")
public class ConditionSimulationResource {

    @ConfigProperty(name = "feature.condition-simulation.enabled", defaultValue = "false")
    boolean simulationEnabled;

    @Inject
    ConditionSimulator conditionSimulator;

    @PUT
    @Path("/strategy")
    public Response setStrategy(@Valid SetSimulatedConditionRequest request) {
        if (!simulationEnabled) {
            throw new NotFoundException();
        }

        conditionSimulator.useStrategy(request.strategy(), request.delaySeconds());
        return Response.noContent().build();
    }
}
