package com.dcava.dcava_backend.controller;

import com.dcava.dcava_backend.DTO.ProductImageDTO;
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

    // 🔹 Obtener todas las imágenes de un producto
    @GetMapping("/products/{id}/images")
    public List<ProductImageDTO> getProductImages(@PathVariable int id) {
        List<ProductImage> images = imageService.getImagesByProduct(id);
        return images.stream().map(ProductImageDTO::new).toList();
    }

    // 🔹 Subir nueva imagen
    @PostMapping("/{productId}/images")
    public ResponseEntity<?> uploadImage(@PathVariable Integer productId,
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

    // 🔹 Eliminar imagen
    @DeleteMapping("/{imageId}/images")
    public ResponseEntity<?> deleteImage(@PathVariable Integer imageId) {
        if (imageService.deleteImage(imageId)) {
            return ResponseEntity.ok("Image deleted");
        }
        return ResponseEntity.status(404).body("Image not found");
    }
}

