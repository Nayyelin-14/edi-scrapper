package com.datasabai.services.scrapper.app;

import com.datasabai.services.scrapper.core.ScrapperService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quarkus Application - CDI Configuration.
 * Produces the ScrapperService singleton for injection.
 */
@ApplicationScoped
public class ScrapperApplication {

    private static final Logger log = LoggerFactory.getLogger(ScrapperApplication.class);

    @Produces
    @Singleton
    public ScrapperService scrapperService() {
        log.info("Initializing ScrapperService...");
        ScrapperService service = new ScrapperService();
        log.info("ScrapperService initialized");
        return service;
    }
}
