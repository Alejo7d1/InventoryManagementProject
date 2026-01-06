package com.dcava.dcava_backend.controller;

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
@RequestMapping("/products")
public class ProductPublicController {

    private final ProductService productService;
    public ProductPublicController(ProductService productService) { this.productService = productService; }


    //Get product by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable int id) {
        return productService.findById(id)
                .<ResponseEntity<?>>map(product -> ResponseEntity.ok(new ProductPublicDTO(product)))
                .orElse(ResponseEntity.status(404).body("Product not found"));
    }

    //Search by text
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam(defaultValue = "") String text,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "0") int page
    ) {
        Page<Product> result =
                productService.search(text, category, page, sort, order);

        Page<ProductPublicDTO> dtoPage = result.map(ProductPublicDTO::new);

        Map<String, Object> response = new HashMap<>();
        response.put("content", dtoPage.getContent());
        response.put("currentPage", dtoPage.getNumber());
        response.put("totalItems", dtoPage.getTotalElements());
        response.put("totalPages", dtoPage.getTotalPages());
        response.put("size", dtoPage.getSize());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/top-selling")
    public ResponseEntity<List<ProductPublicDTO>> getTopSellingProducts(
            @RequestParam(defaultValue = "1") int months
    ) {
        return ResponseEntity.ok(productService.getTopSellingProducts(months));
    }


}

