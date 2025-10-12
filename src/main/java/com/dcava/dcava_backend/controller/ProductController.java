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

    //Public: Search by text
    @GetMapping("/search")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam(defaultValue = "") String text,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<Product> result = productService.search(text, page, sort, order);
        return ResponseEntity.ok(result);
    }

    //Restricted: Create Product
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.save(product));
    }

    //Restricted: Update Product
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable int id, @RequestBody Product updated) {
        try {
            return ResponseEntity.ok(productService.update(id, updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    //Restricted: delete product
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateProduct(@PathVariable int id) {
        if (productService.deactivate(id)) {
            return ResponseEntity.ok("Product deactivated");
        }
        return ResponseEntity.status(404).body("Product not found");
    }

    //Restricted: Update stock
    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable int id, @RequestParam int quantity) {
        if (productService.updateStock(id, quantity)) {
            return ResponseEntity.ok("Stock updated");
        }
        return ResponseEntity.status(404).body("Product not found");
    }
}

