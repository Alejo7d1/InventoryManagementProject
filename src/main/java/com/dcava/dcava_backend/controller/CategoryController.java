package com.dcava.dcava_backend.controller;

import com.dcava.dcava_backend.dto.ProductPublicDTO;
import com.dcava.dcava_backend.model.Product;
import com.dcava.dcava_backend.service.CategoryService;
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

    @GetMapping
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{name}/products")
    public ResponseEntity<List<ProductPublicDTO>> getProductsByCategory(@PathVariable String name) {
        return ResponseEntity.ok(categoryService.getProductsByCategory(name));
    }
}
