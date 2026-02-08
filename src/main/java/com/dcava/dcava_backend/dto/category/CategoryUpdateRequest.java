package com.dcava.dcava_backend.dto.category;

import jakarta.validation.constraints.Size;

public class CategoryUpdateRequest {

    @Size(max = 64)
    private String name;

    @Size(max = 80)
    private String slug;

    private String description;
    private String imageUrl;
    private String status; // "active" / "inactive"

    // getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
