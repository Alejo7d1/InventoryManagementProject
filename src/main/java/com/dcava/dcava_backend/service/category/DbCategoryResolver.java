package com.dcava.dcava_backend.service.category;

import com.dcava.dcava_backend.dto.category.CategoryAdminView;
import com.dcava.dcava_backend.dto.category.CategoryView;
import com.dcava.dcava_backend.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DbCategoryResolver implements CategoryResolver {

    private final CategoryRepository categoryRepository;

    public DbCategoryResolver(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryView resolveOne(String legacyCategoryName) {
        // TODO: implement
        return null;
    }

    @Override
    public Map<String, CategoryView> resolveAll(Collection<String> legacyCategoryNames) {
        if (legacyCategoryNames == null || legacyCategoryNames.isEmpty()) return Map.of();

        // Build normalized keys
        Set<String> keys = legacyCategoryNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());

        if (keys.isEmpty()) return Map.of();

        var found = categoryRepository.findActiveByLowerNames("active", keys);

        Map<String, CategoryView> out = new HashMap<>();
        for (String k : keys) {
            out.put(k, new CategoryView(null, k, slugify(k), null, null)); // fallback placeholder; optional
        }

        for (var c : found) {
            String k = c.getName().trim().toLowerCase(Locale.ROOT);
            out.put(k, new CategoryView(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getImageUrl()));
        }

        return out;
    }

    private String slugify(String input) {
        // Convert a string into a URL-friendly slug
        return input.toLowerCase(java.util.Locale.ROOT)
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9\\-]", "");
    }

    public Map<String, CategoryView> getCatalog() {
        // English comment: Return all enriched categories indexed by normalized key (e.g., slug or name lowercase).
        return categoryRepository.findAll().stream()
                .filter(c -> "active".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toMap(
                        c -> normalizeKey(c.getName(), c.getSlug()),
                        c -> new CategoryView(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getImageUrl()),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private String normalizeKey(String name, String slug) {
        String base = (slug != null && !slug.isBlank()) ? slug : name;
        return base == null ? "" : base.trim().toLowerCase(Locale.ROOT);
    }

    public Map<String, CategoryView> getAllEnriched() {
        return categoryRepository.findAll().stream()
                .filter(c -> "active".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toMap(
                        c -> normalizeKey(c.getName(), c.getSlug()),
                        c -> new CategoryView(
                                c.getId(),
                                c.getName(),
                                c.getSlug(),
                                c.getDescription(),
                                c.getImageUrl() // <-- usa el getter real
                        ),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    @Override
    public List<CategoryAdminView> getAllEnrichedIncludeInactive() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryAdminView(
                        c.getId(),
                        c.getName(),
                        c.getSlug(),
                        c.getDescription(),
                        c.getImageUrl(), // path en DB (o lo que uses)
                        c.getStatus()
                ))
                .toList();
    }

}

