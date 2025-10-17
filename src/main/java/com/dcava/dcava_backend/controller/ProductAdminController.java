package com.dcava.dcava_backend.controller;

import com.dcava.dcava_backend.dto.ProductAdminDTO;
import com.dcava.dcava_backend.dto.ProductPublicDTO;
import com.dcava.dcava_backend.model.Product;
import com.dcava.dcava_backend.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/products")
public class ProductAdminController {

    private final ProductService productService;
    public ProductAdminController(ProductService productService) { this.productService = productService; }

    //Get product by id (with cost)
    @GetMapping("/{id}")
    public ResponseEntity<?> privateGetProductById(@PathVariable int id) {
        return productService.findById(id)
                .<ResponseEntity<?>>map(product -> ResponseEntity.ok(new ProductAdminDTO(product)))
                .orElse(ResponseEntity.status(404).body("Product not found"));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam(defaultValue = "") String text,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<Product> result = productService.adminSearch(text, page, status);
        Page<ProductAdminDTO> dtoPage = result.map(ProductAdminDTO::new);

        Map<String, Object> response = new HashMap<>();
        response.put("content", dtoPage.getContent());
        response.put("currentPage", dtoPage.getNumber());
        response.put("totalItems", dtoPage.getTotalElements());
        response.put("totalPages", dtoPage.getTotalPages());
        response.put("size", dtoPage.getSize());

        return ResponseEntity.ok(response);
    }

    //get deleted products
    @GetMapping("/deleted")
    public ResponseEntity<List<Product>> getDeletedProducts() {
        return ResponseEntity.ok(productService.getAllProductsDesactivated());
    }


    //Create Product
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        return ResponseEntity.ok(productService.save(product));
    }

    //Update Product
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable int id, @RequestBody Product updated) {
        try {
            return ResponseEntity.ok(productService.update(id, updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    //Delete product
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateProduct(@PathVariable int id) {
        if (productService.deactivate(id)) {
            return ResponseEntity.ok("Product deactivated");
        }
        return ResponseEntity.status(404).body("Product not found");
    }

    //Update stock
    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> updateStock(@PathVariable int id, @RequestParam int quantity) {
        if (productService.updateStock(id, quantity)) {
            return ResponseEntity.ok("Stock updated");
        }
        return ResponseEntity.status(404).body("Product not found");
    }


}
