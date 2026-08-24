package com.datasabai.services.scrapper.core.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Represents an element (simple or composite) in an EDI segment.
 * <p>
 * Composite elements have a nested Elements list.
 * Simple elements have type, min/max length, and no nested elements.
 * </p>
 */
public class Element {

    @JsonProperty("Scope")
    private String scope = "";

    @JsonProperty("Position")
    private String position = "";

    @JsonProperty("Element")
    private String element;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Description")
    private String description = "";

    @JsonProperty("Requirement")
    private String requirement = "";

    @JsonProperty("Type")
    private String type = "";

    @JsonProperty("Min")
    private String min = "";

    @JsonProperty("Max")
    private String max = "";

    @JsonProperty("Elements")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Element> elements;

    public Element() {}

    // Getters and setters
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public String getElement() { return element; }
    public void setElement(String element) { this.element = element; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirement() { return requirement; }
    public void setRequirement(String requirement) { this.requirement = requirement; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMin() { return min; }
    public void setMin(String min) { this.min = min; }

    public String getMax() { return max; }
    public void setMax(String max) { this.max = max; }

    public List<Element> getElements() { return elements; }
    public void setElements(List<Element> elements) { this.elements = elements; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Element element = new Element();

        public Builder scope(String scope) { element.scope = scope; return this; }
        public Builder position(String position) { element.position = position; return this; }
        public Builder element(String el) { element.element = el; return this; }
        public Builder name(String name) { element.name = name; return this; }
        public Builder description(String desc) { element.description = desc; return this; }
        public Builder requirement(String req) { element.requirement = req; return this; }
        public Builder type(String type) { element.type = type; return this; }
        public Builder min(String min) { element.min = min; return this; }
        public Builder max(String max) { element.max = max; return this; }
        public Builder elements(List<Element> elements) { element.elements = elements; return this; }

        public Element build() { return element; }
    }
}
