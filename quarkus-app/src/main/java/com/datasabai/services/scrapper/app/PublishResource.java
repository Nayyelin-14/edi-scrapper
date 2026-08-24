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
 * Publish REST endpoint.
 * Maps to POST /api/publish.
 */
@Path("/api/publish")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublishResource {

    private static final Logger log = LoggerFactory.getLogger(PublishResource.class);

    @Inject
    ScrapperService service;

    @POST
    public Response publish(Map<String, String> body) {
        String standard = body.get("standard");
        String revision = body.get("revision");
        String messageType = body.get("messageType");

        if (standard == null || revision == null || messageType == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "standard, revision, and messageType are required")).build();
        }

        try {
            Map<String, String> result = service.publish(standard, revision, messageType);
            return Response.ok(result).build();
        } catch (ScrapperException e) {
            log.error("Publish failed", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Publish failed", "error", e.getMessage())).build();
        }
    }
}
