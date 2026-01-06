package com.dcava.dcava_backend.controller;

import com.dcava.dcava_backend.dto.ProductAdminDTO;
import com.dcava.dcava_backend.model.Product;
import com.dcava.dcava_backend.service.ProductImageService;
import com.dcava.dcava_backend.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/products")
public class ProductAdminController {

    private final ProductService productService;
    private final ProductImageService productImageService;

    public ProductAdminController(ProductService productService, ProductImageService productImageService) { this.productService = productService;
        this.productImageService = productImageService;
    }

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
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order
    ) {
        Page<Product> result = productService.adminSearch(
                text,
                category,
                page,
                status,
                sort,
                order
        );

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
        return ResponseEntity.ok(productService.getAllProductsDeactivated());
    }

    @GetMapping("/low-stock")
    public ResponseEntity<Page<Product>> getLowStockProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5", name = "stockThreshold") int threshold
    ) {
        return  ResponseEntity.ok(productService.getLowStockProducts(page, threshold));
    }


    //add product
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @RequestPart("product") Product product,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        try {
            Product savedProduct = productService.save(product, images);
            if (images == null || images.isEmpty()) {
                productImageService.saveGenericImage(savedProduct.getId());
            }
            return ResponseEntity.ok(savedProduct);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error saving images: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
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

    //Restore product
    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreProduct(@PathVariable int id) {
        if (productService.restore(id)){
            return ResponseEntity.ok("Product restored");
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
