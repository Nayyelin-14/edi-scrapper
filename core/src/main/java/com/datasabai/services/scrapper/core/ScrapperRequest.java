package com.datasabai.services.scrapper.core;

/**
 * Input request for the EDI Spec Scrapper service.
 */
public class ScrapperRequest {

    private String standard;
    private String revision;
    private String messageType;
    private String action; // "scrape", "result", "publish"
    private String format; // "canonical", "jsonschema", "beanio"
    private String delimiter; // custom field delimiter (e.g. "*", "+")
    private String recordTerminator; // custom record terminator (e.g. "~", "'")
    private String componentDelimiter; // composite component delimiter (default ":")
    private String basePackage; // base package for POJO classes (e.g. "com.example.edi.model")
    private String rootClass; // root class simple name (e.g. "FlatEdifactD97aOrdersPo")

    public ScrapperRequest() {
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getRecordTerminator() {
        return recordTerminator;
    }

    public void setRecordTerminator(String recordTerminator) {
        this.recordTerminator = recordTerminator;
    }

    public String getComponentDelimiter() {
        return componentDelimiter;
    }

    public void setComponentDelimiter(String componentDelimiter) {
        this.componentDelimiter = componentDelimiter;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public String getRootClass() {
        return rootClass;
    }

    public void setRootClass(String rootClass) {
        this.rootClass = rootClass;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ScrapperRequest request = new ScrapperRequest();

        public Builder standard(String standard) {
            request.standard = standard;
            return this;
        }

        public Builder revision(String revision) {
            request.revision = revision;
            return this;
        }

        public Builder messageType(String messageType) {
            request.messageType = messageType;
            return this;
        }

        public Builder action(String action) {
            request.action = action;
            return this;
        }

        public Builder format(String format) {
            request.format = format;
            return this;
        }

        public Builder delimiter(String delimiter) {
            request.delimiter = delimiter;
            return this;
        }

        public Builder recordTerminator(String recordTerminator) {
            request.recordTerminator = recordTerminator;
            return this;
        }

        public Builder componentDelimiter(String componentDelimiter) {
            request.componentDelimiter = componentDelimiter;
            return this;
        }

        public Builder basePackage(String basePackage) {
            request.basePackage = basePackage;
            return this;
        }

        public Builder rootClass(String rootClass) {
            request.rootClass = rootClass;
            return this;
        }

        public ScrapperRequest build() {
            return request;
        }
    }
}