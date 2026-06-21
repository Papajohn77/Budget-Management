package gr.aueb.budgetmanagement.application.clients;

import java.time.temporal.ChronoUnit;

import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import gr.aueb.budgetmanagement.application.representations.PiggyBankTotalsRepresentation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@RegisterClientHeaders
@Path("/api/v1/piggy-banks")
@RegisterRestClient(configKey = "piggybank-service")
public interface PiggyBankClient {
    @GET
    @Path("/totals")
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    PiggyBankTotalsRepresentation getTotals(@QueryParam("user_id") Long userId);
}
