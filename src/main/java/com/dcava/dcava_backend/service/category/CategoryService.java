package com.dcava.dcava_backend.service.category;

import com.dcava.dcava_backend.config.AppProperties;
import com.dcava.dcava_backend.dto.category.CategoryAdminView;
import com.dcava.dcava_backend.dto.category.CategoryView;
import com.dcava.dcava_backend.dto.product.ProductPublicDTO;
import com.dcava.dcava_backend.dto.product.ProductPublicDTOEnriched;
import com.dcava.dcava_backend.model.Product;
import com.dcava.dcava_backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CategoryService {
    private final ProductRepository productRepo;
    private final CategoryResolver categoryResolver;
    private final AppProperties appProperties;

    public CategoryService(
            ProductRepository productRepo,
            CategoryResolver categoryResolver,
            AppProperties appProperties
    ) {
        this.productRepo = productRepo;
        this.categoryResolver = categoryResolver;
        this.appProperties = appProperties;
    }


    public List<String> getAllCategories() {
        return productRepo.findDistinctCategories();
    }

    public List<ProductPublicDTO> getProductsByCategory(String category) {
        return productRepo.findByCategoryIgnoreCase(category)
                .stream()
                .map(ProductPublicDTO::new)
                .toList();
    }

    public List<CategoryView> getAllCategoryViews() {

        // 1) Legacy from products
        List<String> legacyCategories = getAllCategories();

        // 2) Enriched catalog (all admin categories)
        Map<String, CategoryView> allEnriched = categoryResolver.getAllEnriched();

        // 3) Resolve legacy -> enriched when possible
        Map<String, CategoryView> resolvedLegacy = categoryResolver.resolveAll(legacyCategories);

        // 4) Merge keys: legacy + catalog, without duplicates
        LinkedHashMap<String, CategoryView> merged = new LinkedHashMap<>();

        // Add catalog first (so new categories appear even with no products)
        merged.putAll(allEnriched);

        // Add legacy resolved (doesn't overwrite catalog entries)
        for (String raw : legacyCategories) {
            if (raw == null) continue;
            String key = raw.trim().toLowerCase(java.util.Locale.ROOT);
            if (key.isBlank()) continue;

            CategoryView cv = resolvedLegacy.get(key);
            if (cv == null) {
                // If legacy doesn't exist in catalog, keep it as a “legacy only” view
                cv = new CategoryView(null, raw.trim(), raw.trim().toLowerCase(java.util.Locale.ROOT), null, null);
            }

            merged.putIfAbsent(normalizeKey(cv.name(), cv.slug()), cv);
        }

        // 5) Apply URL expansion for imageUrl (same as ProductImageService)
        String publicUrl = appProperties.getR2().getPublicUrl().trim();
        String bucket = appProperties.getR2().getBucket().trim();

        return merged.values().stream()
                .map(cv -> {
                    if (cv.imageUrl() == null || cv.imageUrl().isBlank()) return cv;

                    String path = cv.imageUrl().trim();
                    if (path.startsWith("/")) path = path.substring(1);

                    String fullUrl =
                            publicUrl.endsWith("/")
                                    ? publicUrl + bucket + "/" + path
                                    : publicUrl + "/" + path;

                    return new CategoryView(
                            cv.id(),
                            cv.name(),
                            cv.slug(),
                            cv.description(),
                            fullUrl
                    );
                })
                .toList();
    }

    private String normalizeKey(String name, String slug) {
        String base = (slug != null && !slug.isBlank()) ? slug : name;
        if (base == null) return "";
        return base.trim().toLowerCase(java.util.Locale.ROOT);
    }



    public CategoryView getCategoryView(String category) {
        return categoryResolver.resolveOne(category);
    }

    public List<ProductPublicDTOEnriched> getProductsByCategoryEnriched(String category) {

        List<Product> products =
                productRepo.findByCategoryIgnoreCase(category);

        CategoryView categoryView =
                categoryResolver.resolveOne(category);

        return products.stream()
                .map(p -> new ProductPublicDTOEnriched(p, categoryView))
                .toList();
    }

    public List<CategoryAdminView> getAllCategoryViewsIncludeInactive() {

        List<String> legacyCategories = getAllCategories();

        // Admin catalog (active + inactive)
        List<CategoryAdminView> adminAll = categoryResolver.getAllEnrichedIncludeInactive();

        // Resolve legacy -> CategoryView (id null when not enriched)
        Map<String, CategoryView> resolvedLegacy = categoryResolver.resolveAll(legacyCategories);

        //Merge by normalized key (lowercase slug/name), admin wins (has id/status)
        LinkedHashMap<String, CategoryAdminView> merged = new LinkedHashMap<>();

        for (CategoryAdminView c : adminAll) {
            String key = normalizeKey(c.name(), c.slug());
            merged.put(key, c);
        }

        for (CategoryView cv : resolvedLegacy.values()) {
            String key = normalizeKey(cv.name(), cv.slug());
            merged.putIfAbsent(key, new CategoryAdminView(
                    null,
                    cv.name(),
                    cv.slug(),
                    cv.description(),
                    cv.imageUrl(),
                    "legacy" // or "active" if you prefer; better to label it
            ));
        }

        String publicUrl = appProperties.getR2().getPublicUrl().trim();
        String bucket = appProperties.getR2().getBucket().trim();

        return merged.values().stream()
                .map(cv -> {
                    if (cv.imageUrl() == null || cv.imageUrl().isBlank()) return cv;

                    String path = cv.imageUrl().trim();
                    if (path.startsWith("/")) path = path.substring(1);

                    String fullUrl =
                            publicUrl.endsWith("/")
                                    ? publicUrl + bucket + "/" + path
                                    : publicUrl + "/" + path;

                    return new CategoryAdminView(
                            cv.id(),
                            cv.name(),
                            cv.slug(),
                            cv.description(),
                            fullUrl,
                            cv.status()
                    );
                })
                .toList();
    }




}


