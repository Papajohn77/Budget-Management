package gr.aueb.budgetmanagement.presentation.api.resources;

import java.net.URI;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("/")
public class RootResource {
    
    @GET
    @Operation(hidden = true)
    public Response redirectToSwagger() {
        return Response.temporaryRedirect(URI.create("/q/swagger-ui")).build();
    }
}
