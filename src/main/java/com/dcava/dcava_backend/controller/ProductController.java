package com.dcava.dcava_backend.controller;

import com.dcava.dcava_backend.model.Product;
import com.dcava.dcava_backend.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    public ProductController(ProductService productService) { this.productService = productService; }

    // public: get product by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable int id) {
        return productService.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).body("Product not found"));
    }

    // 🔹 Público: búsqueda con texto, orden y paginación
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam(defaultValue = "") String text,
            @RequestParam(defaultValue = "stock_asc") String sortOrder,
            @RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(productService.search(text, page, sortOrder));
    }

    // 🔹 Restringido: crear producto
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.save(product));
    }

    // 🔹 Restringido: actualizar producto
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable int id, @RequestBody Product updated) {
        try {
            return ResponseEntity.ok(productService.update(id, updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // 🔹 Restringido: desactivar (borrado lógico)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateProduct(@PathVariable int id) {
        if (productService.deactivate(id)) {
            return ResponseEntity.ok("Product deactivated");
        }
        return ResponseEntity.status(404).body("Product not found");
    }

    // 🔹 Restringido: actualizar stock
    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable int id, @RequestParam int quantity) {
        if (productService.updateStock(id, quantity)) {
            return ResponseEntity.ok("Stock updated");
        }
        return ResponseEntity.status(404).body("Product not found");
    }
}

