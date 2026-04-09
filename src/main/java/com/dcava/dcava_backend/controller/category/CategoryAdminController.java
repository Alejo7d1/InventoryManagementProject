package com.dcava.dcava_backend.controller.category;

import com.dcava.dcava_backend.dto.category.CategoryAdminView;
import com.dcava.dcava_backend.dto.category.CategoryCreateRequest;
import com.dcava.dcava_backend.dto.category.CategoryUpdateRequest;
import com.dcava.dcava_backend.dto.category.CategoryView;
import com.dcava.dcava_backend.service.category.CategoryAdminService;
import com.dcava.dcava_backend.service.category.CategoryImageService;
import com.dcava.dcava_backend.service.category.CategoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/admin/categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryAdminController {

    private final CategoryImageService categoryImageService;
    private final CategoryAdminService adminService;
    private final CategoryService categoryService;

    public CategoryAdminController(
            CategoryImageService categoryImageService,
            CategoryAdminService adminService,
            CategoryService categoryService) {
        this.categoryImageService = categoryImageService;
        this.adminService = adminService;
        this.categoryService = categoryService;
    }

    @GetMapping("/enriched")
    public ResponseEntity<List<CategoryAdminView>> getCategoriesEnrichedAdmin() {
        return ResponseEntity.ok(categoryService.getAllCategoryViewsIncludeInactive());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CategoryCreateRequest req) {
        try {
            return ResponseEntity.ok(adminService.create(req));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody CategoryUpdateRequest req) {
        try {
            return ResponseEntity.ok(adminService.update(id, req));
        } catch (IllegalArgumentException e) {
            String msg = e.getMessage();
            if ("Category not found".equals(msg)) return ResponseEntity.status(404).body(msg);
            return ResponseEntity.badRequest().body(msg);
        }
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @PathVariable Integer id,
            @RequestPart("image") MultipartFile image
    ) {
        try {
            CategoryView updated = categoryImageService.uploadCategoryImage(id, image);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error reading image: " + e.getMessage());
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if ("Category not found".equals(msg)) return ResponseEntity.status(404).body(msg);
            return ResponseEntity.badRequest().body(msg);
        }
    }
}
