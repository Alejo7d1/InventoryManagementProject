package com.dcava.dcava_backend.service;

import com.dcava.dcava_backend.model.Product;
import com.dcava.dcava_backend.model.ProductImage;
import com.dcava.dcava_backend.repository.ProductImageRepository;
import com.dcava.dcava_backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ProductImageService {

    @Value("${app.upload.dir}")
    private String baseUploadDir;

    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;

    public ProductImageService(ProductImageRepository imageRepository, ProductRepository productRepository) {
        this.imageRepository = imageRepository;
        this.productRepository = productRepository;
    }

    // 🔹 Obtener imágenes por producto
    public List<ProductImage> getImagesByProduct(Integer productId) {
        return imageRepository.findByProductId(productId);
    }

    // 🔹 Guardar imagen en disco y base de datos
    public ProductImage saveImage(Integer productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        String category = product.getCategory() != null ? product.getCategory() : "uncategorized";
        String fileName = productId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // 🧱 Crear directorio si no existe
        Path uploadPath = Paths.get(baseUploadDir, category).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        // 📁 Guardar archivo
        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        // 🧾 Guardar en BD
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setFileName(fileName);
        image.setFilePath("/uploads/" + category + "/" + fileName);

        return imageRepository.save(image);
    }

    // 🔹 Eliminar imagen física y de BD
    public boolean deleteImage(Integer imageId) {
        return imageRepository.findById(imageId).map(image -> {
            try {
                Path filePath = Paths.get("." + image.getFilePath()).toAbsolutePath();
                Files.deleteIfExists(filePath);
            } catch (IOException ignored) {}

            imageRepository.delete(image);
            return true;
        }).orElse(false);
    }
}

