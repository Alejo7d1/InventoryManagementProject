package com.dcava.dcava_backend.dto.category;

public record CategoryAdminView(
        Integer id,
        String name,
        String slug,
        String description,
        String imageUrl,
        String status
) {}

