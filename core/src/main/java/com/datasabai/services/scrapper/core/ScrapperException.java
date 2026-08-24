package com.datasabai.services.scrapper.core;

/**
 * Custom exception for the EDI Spec Scrapper service.
 */
public class ScrapperException extends Exception {

    private final String errorCode;

    public ScrapperException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ScrapperException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
