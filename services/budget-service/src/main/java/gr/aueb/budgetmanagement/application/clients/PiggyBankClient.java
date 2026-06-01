package gr.aueb.budgetmanagement.application.clients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import gr.aueb.budgetmanagement.application.representations.PiggyBankTotalsRepresentation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/api/v1/piggy-banks")
@RegisterRestClient(configKey = "piggybank-service")
public interface PiggyBankClient {
    @GET
    @Path("/totals")
    PiggyBankTotalsRepresentation getTotals(@QueryParam("user_id") Long userId);
}
