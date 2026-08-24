package com.datasabai.services.scrapper.app;

import com.datasabai.services.scrapper.core.ScrapperService;
import com.datasabai.services.scrapper.core.model.EdiSpec;
import com.datasabai.services.scrapper.core.scraper.ScrapeProgress;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * SSE endpoint for real-time scraping progress.
 * Uses JAX-RS SSE API for named events (progress, done, error).
 */
@Path("/api/scrape")
public class ScrapeSSEResource {

    private static final Logger log = LoggerFactory.getLogger(ScrapeSSEResource.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Inject
    ScrapperService service;

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void scrape(@QueryParam("standard") String standard,
                       @QueryParam("revision") String revision,
                       @QueryParam("messageType") String messageType,
                       @Context SseEventSink sink,
                       @Context Sse sse) {

        if (standard == null || revision == null || messageType == null) {
            sink.send(sse.newEventBuilder()
                    .name("error")
                    .data(toJson(Map.of("message", "All fields are required")))
                    .build());
            sink.close();
            return;
        }

        executor.submit(() -> {
            try {
                EdiSpec result = service.scrape(standard, revision, messageType,
                        (ScrapeProgress progress) -> {
                            if (!sink.isClosed()) {
                                try {
                                    sink.send(sse.newEventBuilder()
                                            .name("progress")
                                            .data(objectMapper.writeValueAsString(progress))
                                            .build());
                                } catch (Exception e) {
                                    log.warn("Failed to send progress event", e);
                                }
                            }
                        });

                if (!sink.isClosed()) {
                    String cleanRevision = revision.replaceAll("\\s+\\(\\d+\\)$", "");
                    sink.send(sse.newEventBuilder()
                            .name("done")
                            .data(toJson(Map.of("revision", cleanRevision, "messageType", messageType)))
                            .build());
                }
            } catch (Exception e) {
                log.error("Scraping error", e);
                if (!sink.isClosed()) {
                    sink.send(sse.newEventBuilder()
                            .name("error")
                            .data(toJson(Map.of("message", e.getMessage() != null ? e.getMessage() : "Unknown error")))
                            .build());
                }
            } finally {
                if (!sink.isClosed()) {
                    sink.close();
                }
            }
        });
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"message\":\"Serialization error\"}";
        }
    }
}
