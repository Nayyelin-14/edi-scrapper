package com.datasabai.services.scrapper.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level specification envelope for EDI documents.
 * Maps to the canonical JSON output structure with PascalCase keys.
 */
public class EdiSpec {

    @JsonProperty("Standard")
    private String standard;

    @JsonProperty("Revision")
    private String revision;

    @JsonProperty("Document")
    private String document;

    @JsonProperty("Segments")
    private List<Segment> segments;

    public EdiSpec() {
        this.segments = new ArrayList<>();
    }

    public EdiSpec(String standard, String revision, String document) {
        this.standard = standard;
        this.revision = revision;
        this.document = document;
        this.segments = new ArrayList<>();
    }

    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }

    public String getRevision() { return revision; }
    public void setRevision(String revision) { this.revision = revision; }

    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }

    public List<Segment> getSegments() { return segments; }
    public void setSegments(List<Segment> segments) { this.segments = segments; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final EdiSpec spec = new EdiSpec();

        public Builder standard(String standard) { spec.standard = standard; return this; }
        public Builder revision(String revision) { spec.revision = revision; return this; }
        public Builder document(String document) { spec.document = document; return this; }
        public Builder segments(List<Segment> segments) { spec.segments = segments; return this; }

        public EdiSpec build() { return spec; }
    }
}
