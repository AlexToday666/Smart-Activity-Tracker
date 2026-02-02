package com.example.ActivityTracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ApiKeyCreateRequestDto {
    @NotBlank(message = "name is required")
    @Size(max = 120, message = "name must be at most 120 characters")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
