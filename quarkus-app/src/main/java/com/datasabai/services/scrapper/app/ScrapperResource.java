package com.datasabai.services.scrapper.app;

import com.datasabai.services.scrapper.core.ScrapperException;
import com.datasabai.services.scrapper.core.ScrapperRequest;
import com.datasabai.services.scrapper.core.ScrapperResult;
import com.datasabai.services.scrapper.core.ScrapperService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST API - standard endpoints (health, config-schema, execute).
 */
@Path("/api/scrapper")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ScrapperResource {

    private static final Logger log = LoggerFactory.getLogger(ScrapperResource.class);

    private static final String SERVICE_NAME = "edi-spec-scrapper";
    private static final String SERVICE_VERSION = "1.0.0";
    private static final String SERVICE_DESCRIPTION =
            "EDI Specification Scraper - EDIFACT & X12 scraping, JSON Schema & BeanIO XML generation";

    @Inject
    ScrapperService service;

    @POST
    @Path("/execute")
    public Response execute(ScrapperRequest request) {
        log.info("Received execute request: {} {} {}", request.getStandard(),
                request.getRevision(), request.getMessageType());
        try {
            ScrapperResult result = service.execute(request);
            return Response.ok(result).build();
        } catch (ScrapperException e) {
            log.error("Service execution failed", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage())).build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid request", e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage())).build();
        }
    }

    @GET
    @Path("/health")
    public Response health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", SERVICE_NAME);
        health.put("version", SERVICE_VERSION);
        health.put("description", SERVICE_DESCRIPTION);
        return Response.ok(health).build();
    }

    @GET
    @Path("/config-schema")
    public Response getConfigSchema() {
        Map<String, String> schema = new LinkedHashMap<>();
        schema.put("standard", "EDI standard: EDIFACT or X12 (string, required)");
        schema.put("revision", "EDI revision/version (string, required)");
        schema.put("messageType", "Message type or transaction set code (string, required)");
        schema.put("outputDir", "Output directory for scraped files (string, default: output)");
        schema.put("delimiter", "Custom field delimiter (string, default: '+' for EDIFACT, '*' for X12)");
        schema.put("recordTerminator", "Custom record terminator (string, default: \"'\" for EDIFACT, '~' for X12)");
        schema.put("componentDelimiter", "Composite component delimiter (string, default: ':')");
        schema.put("basePackage", "Base package for POJO classes, enables CompositeTypeHandler mode (string, e.g. 'com.example.edi.model')");
        return Response.ok(schema).build();
    }
}
