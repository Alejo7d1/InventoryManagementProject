package com.dcava.dcava_backend.controller.category;

import com.dcava.dcava_backend.dto.category.CategoryView;
import com.dcava.dcava_backend.dto.product.ProductPublicDTO;
import com.dcava.dcava_backend.dto.product.ProductPublicDTOEnriched;
import com.dcava.dcava_backend.service.category.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // Legacy
    @GetMapping
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // Legacy
    @GetMapping("/{name}/products")
    public ResponseEntity<List<ProductPublicDTO>> getProductsByCategory(@PathVariable String name) {
        return ResponseEntity.ok(categoryService.getProductsByCategory(name));
    }

    // New version
    @GetMapping("/enriched")
    public ResponseEntity<List<CategoryView>> getCategoriesEnriched() {
        return ResponseEntity.ok(categoryService.getAllCategoryViews());
    }

    // New version
    @GetMapping("/{name}/products/enriched")
    public ResponseEntity<List<ProductPublicDTOEnriched>> getProductsByCategoryEnriched(@PathVariable String name) {
        return ResponseEntity.ok(categoryService.getProductsByCategoryEnriched(name));
    }
}
