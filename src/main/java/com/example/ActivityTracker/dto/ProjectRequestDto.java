package com.example.ActivityTracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ProjectRequestDto {
    @NotBlank(message = "name is required")
    @Size(max = 160, message = "name must be at most 160 characters")
    private String name;

    @NotBlank(message = "slug is required")
    @Pattern(regexp = "^[a-z0-9][a-z0-9-]{1,80}$", message = "slug must be lowercase kebab-case")
    private String slug;

    @Size(max = 500, message = "description must be at most 500 characters")
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
