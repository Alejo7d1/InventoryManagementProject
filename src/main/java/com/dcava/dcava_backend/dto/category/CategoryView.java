package com.dcava.dcava_backend.dto.category;

public record CategoryView(
        Integer id,
        String name,
        String slug,
        String description,
        String imageUrl
) {}
