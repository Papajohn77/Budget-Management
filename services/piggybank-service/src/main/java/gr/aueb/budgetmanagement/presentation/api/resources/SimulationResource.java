package gr.aueb.budgetmanagement.presentation.api.resources;

import gr.aueb.budgetmanagement.infrastructure.simulation.SimulationState;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/api/v1/simulate")
@ApplicationScoped
public class SimulationResource {

    @Inject
    SimulationState state;

    @POST
    @Path("/healthy")
    public Response setHealthy() {
        state.setHealthy(true);
        return Response.ok().build();
    }

    @POST
    @Path("/unhealthy")
    public Response setUnhealthy() {
        state.setHealthy(false);
        return Response.ok().build();
    }
}
