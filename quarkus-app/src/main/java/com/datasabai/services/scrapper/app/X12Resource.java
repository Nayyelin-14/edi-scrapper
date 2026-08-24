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
 * X12-specific REST endpoints.
 * Maps to /api/x12/revisions and /api/x12/transaction-sets/:revision.
 */
@Path("/api/x12")
@Produces(MediaType.APPLICATION_JSON)
public class X12Resource {

    private static final Logger log = LoggerFactory.getLogger(X12Resource.class);

    @Inject
    ScrapperService service;

    @GET
    @Path("/revisions")
    public Response getRevisions() {
        try {
            return Response.ok(service.getX12Revisions()).build();
        } catch (Exception e) {
            log.error("Error fetching X12 revisions", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/transaction-sets/{revision}")
    public Response getTransactionSets(@PathParam("revision") String revision) {
        try {
            return Response.ok(service.getX12TransactionSets(revision)).build();
        } catch (ScrapperException e) {
            log.error("Error fetching X12 transaction sets", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", e.getMessage())).build();
        }
    }
}
