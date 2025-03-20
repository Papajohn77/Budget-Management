package gr.aueb.budgetmanagement.presentation.api.exceptions;

import java.util.List;

import gr.aueb.budgetmanagement.domain.exceptions.NotFoundDomainException;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import gr.aueb.budgetmanagement.application.exceptions.AlreadyExistsException;
import gr.aueb.budgetmanagement.application.exceptions.ForbiddenException;
import gr.aueb.budgetmanagement.application.exceptions.InvalidCredentialsException;
import gr.aueb.budgetmanagement.application.exceptions.NotFoundException;
import gr.aueb.budgetmanagement.domain.exceptions.AlreadyExistsDomainException;
import gr.aueb.budgetmanagement.domain.exceptions.ForbiddenOperationDomainException;
import gr.aueb.budgetmanagement.domain.exceptions.InvalidDomainArgumentException;
import gr.aueb.budgetmanagement.presentation.api.exceptions.representations.ErrorResponseRepresentation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class GlobalExceptionHandler {
    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);
    
    @ServerExceptionMapper
    public Response handleConstraintViolationException(ConstraintViolationException e) {
        List<String> violations = e.getConstraintViolations()
            .stream()
            .map(ConstraintViolation::getMessage)
            .toList();

        ErrorResponseRepresentation error = ErrorResponseRepresentation.create(
            Response.Status.BAD_REQUEST.getStatusCode(),
            "Bad Request: " + String.join("; ", violations)
        );
        
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }
    
    @ServerExceptionMapper
    public Response handleDomainBadRequest(InvalidDomainArgumentException e) {
        ErrorResponseRepresentation error = ErrorResponseRepresentation.create(
            Response.Status.BAD_REQUEST.getStatusCode(),
            "Bad Request: " + e.getMessage()
        );
        
        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }
    
    @ServerExceptionMapper
    public Response handleApplicationUnauthorized(InvalidCredentialsException e) {
        ErrorResponseRepresentation error = ErrorResponseRepresentation.create(
            Response.Status.UNAUTHORIZED.getStatusCode(),
            "Unauthorized: " + e.getMessage()
        );
        
        return Response.status(Response.Status.UNAUTHORIZED)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }
    
    @ServerExceptionMapper
    public Response handleApplicationForbidden(ForbiddenException e) {
        ErrorResponseRepresentation error = ErrorResponseRepresentation.create(
            Response.Status.FORBIDDEN.getStatusCode(),
            "Forbidden: " + e.getMessage()
        );
        
        return Response.status(Response.Status.FORBIDDEN)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }
    
    @ServerExceptionMapper
    public Response handleDomainForbidden(ForbiddenOperationDomainException e) {
        ErrorResponseRepresentation error = ErrorResponseRepresentation.create(
            Response.Status.FORBIDDEN.getStatusCode(),
            "Forbidden: " + e.getMessage()
        );
        
        return Response.status(Response.Status.FORBIDDEN)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }
    
    @ServerExceptionMapper
    public Response handleApplicationNotFound(NotFoundException e) {
        ErrorResponseRepresentation error = ErrorResponseRepresentation.create(
            Response.Status.NOT_FOUND.getStatusCode(),
            "Not Found: " + e.getMessage()
        );

        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }

    @ServerExceptionMapper
    public Response handleDomainNotFound(NotFoundDomainException e) {
        ErrorResponseRepresentation error = ErrorResponseRepresentation.create(
            Response.Status.NOT_FOUND.getStatusCode(),
            "Not Found: " + e.getMessage()
        );

        return Response.status(Response.Status.NOT_FOUND)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }
    
    @ServerExceptionMapper
    public Response handleApplicationConflict(AlreadyExistsException e) {
        ErrorResponseRepresentation error = ErrorResponseRepresentation.create(
            Response.Status.CONFLICT.getStatusCode(),
            "Conflict: " + e.getMessage()
        );
        
        return Response.status(Response.Status.CONFLICT)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }
    
    @ServerExceptionMapper
    public Response handleDomainConflict(AlreadyExistsDomainException e) {
        ErrorResponseRepresentation error = ErrorResponseRepresentation.create(
            Response.Status.CONFLICT.getStatusCode(),
            "Conflict: " + e.getMessage()
        );
        
        return Response.status(Response.Status.CONFLICT)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }
    
    @ServerExceptionMapper
    public Response handleInternalServerError(Exception e) {
        LOG.error("Unhandled exception", e);
        
        ErrorResponseRepresentation error = ErrorResponseRepresentation.create(
            Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
            "Internal Server Error"
        );
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .type(MediaType.APPLICATION_JSON)
            .entity(error)
            .build();
    }
}