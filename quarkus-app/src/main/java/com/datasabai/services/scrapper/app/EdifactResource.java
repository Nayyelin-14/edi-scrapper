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
 * EDIFACT-specific REST endpoints.
 * Maps to /api/edifact/revisions and /api/edifact/message-types/:revision.
 */
@Path("/api/edifact")
@Produces(MediaType.APPLICATION_JSON)
public class EdifactResource {

    private static final Logger log = LoggerFactory.getLogger(EdifactResource.class);

    @Inject
    ScrapperService service;

    @GET
    @Path("/revisions")
    public Response getRevisions() {
        try {
            return Response.ok(service.getEdifactRevisions()).build();
        } catch (Exception e) {
            log.error("Error fetching EDIFACT revisions", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/message-types/{revision}")
    public Response getMessageTypes(@PathParam("revision") String revision) {
        try {
            return Response.ok(service.getEdifactMessageTypes(revision)).build();
        } catch (ScrapperException e) {
            log.error("Error fetching EDIFACT message types", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }
}
