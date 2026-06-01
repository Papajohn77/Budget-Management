package gr.aueb.budgetmanagement.application.clients;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/api/v1/users")
@RegisterRestClient(configKey = "identity-service")
public interface IdentityClient {
    @GET
    UserIdRepresentation findByEmail(@QueryParam("email") String email);
}
