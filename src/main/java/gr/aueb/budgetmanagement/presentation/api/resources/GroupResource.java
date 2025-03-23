package gr.aueb.budgetmanagement.presentation.api.resources;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import gr.aueb.budgetmanagement.application.commands.CreateGroupCommand;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.representations.GroupRepresentation;
import gr.aueb.budgetmanagement.application.representations.GroupsRepresentation;
import gr.aueb.budgetmanagement.application.services.GroupService;
import gr.aueb.budgetmanagement.presentation.api.requests.CreateGroupRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/api/v1/groups")
@SecurityRequirement(name = "JWT")
public class GroupResource {
    private final GroupService groupService;

    public GroupResource(GroupService groupService) {
        this.groupService = groupService;
    }

    @POST
    public Response createGroup(@Context SecurityContext ctx, CreateGroupRequest request) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        CreateGroupCommand command = new CreateGroupCommand(request.name(), authenticatedUserId);
        GroupRepresentation result = groupService.createGroup(command);

        return Response
            .status(Response.Status.CREATED)
            .entity(result)
            .header("Location", "/api/v1/groups/" + result.id())
            .build();
    }

    @GET
    public Response getGroups(@Context SecurityContext ctx) {
        JsonWebToken jwt = (JsonWebToken) ctx.getUserPrincipal();
        if (jwt == null) {
            throw new InvalidCredentialsException("Missing Authorization header with JWT token");
        }
        Long authenticatedUserId = Long.valueOf(jwt.getClaim("user_id").toString());

        GroupsRepresentation result = groupService.getGroups(authenticatedUserId);

        return Response
            .status(Response.Status.OK)
            .entity(result)
            .build();
    }
}
