package com.dcava.dcava_backend.controller;

import com.dcava.dcava_backend.dto.ProductImageDTO;
import com.dcava.dcava_backend.model.ProductImage;
import com.dcava.dcava_backend.service.ProductImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductImageController {

    private final ProductImageService imageService;

    public ProductImageController(ProductImageService imageService) {
        this.imageService = imageService;
    }

    //public: list all product images
    @GetMapping("/{productId}/images")
    public List<ProductImageDTO> getProductImages(@PathVariable int productId) {
        List<ProductImage> images = imageService.getImagesByProduct(productId);
        return images.stream().map(ProductImageDTO::new).toList();
    }

    //restricted: upload imagen (form-data) -> productId and file
    @PostMapping(value = "/images", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadImage(@RequestParam("productId") Integer productId,
                                         @RequestParam("file") MultipartFile file) {
        try {
            ProductImage image = imageService.saveImage(productId, file);
            return ResponseEntity.ok(image);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error saving image: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    // restricted: delete by id
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<?> deleteImage(@PathVariable Integer imageId) {
        if (imageService.deleteImage(imageId)) {
            return ResponseEntity.ok("Image deleted");
        }
        return ResponseEntity.status(404).body("Image not found");
    }
}

