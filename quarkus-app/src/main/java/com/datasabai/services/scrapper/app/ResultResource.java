package com.datasabai.services.scrapper.app;

import com.datasabai.services.scrapper.core.ScrapperException;
import com.datasabai.services.scrapper.core.ScrapperService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Result retrieval REST endpoint.
 * Maps to /api/result/:revision/:messageType.
 */
@Path("/api/result")
@Produces(MediaType.APPLICATION_JSON)
public class ResultResource {

    private static final Logger log = LoggerFactory.getLogger(ResultResource.class);

    @Inject
    ScrapperService service;

    @GET
    @Path("/{revision}/{messageType}")
    public Response getResult(@PathParam("revision") String revision,
                               @PathParam("messageType") String messageType,
                               @QueryParam("format") String format) {
        try {
            if ("jsonschema".equals(format)) {
                return Response.ok(service.getResultAsSchema(revision, messageType)).build();
            } else if ("beanio".equals(format)) {
                String beanioXml = service.getResultAsBeanioXml(revision, messageType);
                return Response.ok(beanioXml, MediaType.APPLICATION_XML).build();
            } else {
                return Response.ok(service.getResult(revision, messageType)).build();
            }
        } catch (ScrapperException e) {
            log.error("Result not found", e);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", "Result not found", "error", e.getMessage())).build();
        }
    }
}
