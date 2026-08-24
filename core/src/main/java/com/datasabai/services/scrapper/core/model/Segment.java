package com.datasabai.services.scrapper.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a segment or segment group in an EDI specification.
 * <p>
 * A segment has Elements (leaf data fields).
 * A group has Segments (nested segments/groups).
 * When used as a group, the Segments field is non-null and Elements is omitted.
 * </p>
 */
public class Segment {

    @JsonProperty("Min")
    private String min = "0";

    @JsonProperty("Max")
    private String max = "1";

    @JsonProperty("Scope")
    private String scope = "";

    @JsonProperty("Position")
    private String position = "";

    @JsonProperty("Segment")
    private String segment;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Description")
    private String description = "";

    @JsonProperty("Requirement")
    private String requirement = "";

    @JsonProperty("Elements")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Element> elements;

    @JsonProperty("Segments")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Segment> segments;

    public Segment() {}

    // Getters and setters
    public String getMin() { return min; }
    public void setMin(String min) { this.min = min; }

    public String getMax() { return max; }
    public void setMax(String max) { this.max = max; }

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getSegment() { return segment; }
    public void setSegment(String segment) { this.segment = segment; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirement() { return requirement; }
    public void setRequirement(String requirement) { this.requirement = requirement; }

    public List<Element> getElements() { return elements; }
    public void setElements(List<Element> elements) { this.elements = elements; }

    public List<Segment> getSegments() { return segments; }
    public void setSegments(List<Segment> segments) { this.segments = segments; }

    /**
     * Creates a segment builder (with Elements list initialized).
     */
    public static Builder segmentBuilder() {
        Builder b = new Builder();
        b.segment.elements = new ArrayList<>();
        return b;
    }

    /**
     * Creates a group builder (with Segments list initialized, no Elements).
     */
    public static Builder groupBuilder() {
        Builder b = new Builder();
        b.segment.segments = new ArrayList<>();
        return b;
    }

    public static class Builder {
        private final Segment segment = new Segment();

        public Builder min(String min) { segment.min = min; return this; }
        public Builder max(String max) { segment.max = max; return this; }
        public Builder scope(String scope) { segment.scope = scope; return this; }
        public Builder position(String position) { segment.position = position; return this; }
        public Builder segment(String seg) { segment.segment = seg; return this; }
        public Builder name(String name) { segment.name = name; return this; }
        public Builder description(String desc) { segment.description = desc; return this; }
        public Builder requirement(String req) { segment.requirement = req; return this; }
        public Builder elements(List<Element> elements) { segment.elements = elements; return this; }
        public Builder segments(List<Segment> segments) { segment.segments = segments; return this; }

        public Segment build() { return segment; }
    }
}
