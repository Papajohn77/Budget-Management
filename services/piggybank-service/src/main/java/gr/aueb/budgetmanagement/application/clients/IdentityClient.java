package gr.aueb.budgetmanagement.application.clients;

import java.time.temporal.ChronoUnit;

import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/api/v1/users")
@RegisterRestClient(configKey = "identity-service")
public interface IdentityClient {
    @GET
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    UserIdRepresentation findByEmail(@QueryParam("email") String email);
}
