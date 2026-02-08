package com.dcava.dcava_backend.service.category;


import com.dcava.dcava_backend.dto.category.CategoryCreateRequest;
import com.dcava.dcava_backend.dto.category.CategoryUpdateRequest;
import com.dcava.dcava_backend.dto.category.CategoryView;
import com.dcava.dcava_backend.model.Category;
import com.dcava.dcava_backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class CategoryAdminService {

    private final CategoryRepository repo;

    public CategoryAdminService(CategoryRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public CategoryView create(CategoryCreateRequest req) {
        String name = req.getName().trim();

        if (repo.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Category name already exists");
        }

        String slug = (req.getSlug() == null || req.getSlug().trim().isBlank())
                ? slugify(name)
                : slugify(req.getSlug().trim());

        if (repo.existsBySlugIgnoreCase(slug)) {
            throw new IllegalArgumentException("Category slug already exists");
        }

        Category c = new Category();
        c.setName(name);
        c.setSlug(slug);
        c.setDescription(req.getDescription());
        c.setImageUrl(req.getImageUrl());
        c.setStatus("active");

        Category saved = repo.save(c);
        return toView(saved);
    }

    @Transactional
    public CategoryView update(Integer id, CategoryUpdateRequest req) {
        Category c = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        if (req.getName() != null && !req.getName().trim().isBlank()) {
            String newName = req.getName().trim();
            if (!newName.equalsIgnoreCase(c.getName()) && repo.existsByNameIgnoreCase(newName)) {
                throw new IllegalArgumentException("Category name already exists");
            }
            c.setName(newName);
        }

        if (req.getSlug() != null && !req.getSlug().trim().isBlank()) {
            String newSlug = slugify(req.getSlug().trim());
            if (!newSlug.equalsIgnoreCase(c.getSlug()) && repo.existsBySlugIgnoreCase(newSlug)) {
                throw new IllegalArgumentException("Category slug already exists");
            }
            c.setSlug(newSlug);
        }

        if (req.getDescription() != null) c.setDescription(req.getDescription());
        if (req.getImageUrl() != null) c.setImageUrl(req.getImageUrl());
        if (req.getStatus() != null && !req.getStatus().trim().isBlank()) c.setStatus(req.getStatus().trim());

        return toView(repo.save(c));
    }

    private CategoryView toView(Category c) {
        return new CategoryView(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getImageUrl());
    }

    private String slugify(String input) {
        // English comments: create URL-friendly identifiers
        return input.toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9\\-]", "");
    }
}
