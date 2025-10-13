package com.dcava.dcava_backend.controller;

import com.dcava.dcava_backend.dto.ProductPublicDTO;
import com.dcava.dcava_backend.model.Product;
import com.dcava.dcava_backend.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class PublicProductController {

    private final ProductService productService;
    public PublicProductController(ProductService productService) { this.productService = productService; }


    //Get product by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable int id) {
        return productService.findById(id)
                .<ResponseEntity<?>>map(product -> ResponseEntity.ok(new ProductPublicDTO(product)))
                .orElse(ResponseEntity.status(404).body("Product not found"));
    }

    //Search by text
    @GetMapping("/search")
    public ResponseEntity<Page<ProductPublicDTO>> searchProducts(
            @RequestParam(defaultValue = "") String text,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<Product> result = productService.search(text, page, sort, order);
        Page<ProductPublicDTO> dtoPage = result.map(ProductPublicDTO::new);
        return ResponseEntity.ok(dtoPage);
    }
}

