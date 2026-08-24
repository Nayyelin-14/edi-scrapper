package com.datasabai.services.scrapper.core;

import com.datasabai.services.scrapper.core.model.EdiSpec;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Output result from the EDI Spec Scrapper service.
 */
public class ScrapperResult {

    private EdiSpec spec;
    private ObjectNode jsonSchema;
    private String beanioXml;
    private String output;
    private boolean success;
    private String message;

    public ScrapperResult() {}

    public EdiSpec getSpec() { return spec; }
    public void setSpec(EdiSpec spec) { this.spec = spec; }

    public ObjectNode getJsonSchema() { return jsonSchema; }
    public void setJsonSchema(ObjectNode jsonSchema) { this.jsonSchema = jsonSchema; }

    public String getBeanioXml() { return beanioXml; }
    public void setBeanioXml(String beanioXml) { this.beanioXml = beanioXml; }

    public String getOutput() { return output; }
    public void setOutput(String output) { this.output = output; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ScrapperResult result = new ScrapperResult();

        public Builder spec(EdiSpec spec) { result.spec = spec; return this; }
        public Builder jsonSchema(ObjectNode jsonSchema) { result.jsonSchema = jsonSchema; return this; }
        public Builder beanioXml(String beanioXml) { result.beanioXml = beanioXml; return this; }
        public Builder output(String output) { result.output = output; return this; }
        public Builder success(boolean success) { result.success = success; return this; }
        public Builder message(String message) { result.message = message; return this; }

        public ScrapperResult build() { return result; }
    }
}
