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

    //Get images by product
    public List<ProductImage> getImagesByProduct(Integer productId) {
        return imageRepository.findByProductId(productId);
    }

    //Save image
    public ProductImage saveImage(Integer productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        String category = product.getCategory() != null ? product.getCategory() : "uncategorized";

        // Temp save in DB
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setFileName("temp");
        image.setFilePath("temp");

        ProductImage savedImage = imageRepository.save(image);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        //Save image
        String newFileName = "image_" + savedImage.getId() + "_product_" + productId + fileExtension;

        //Create directory
        Path uploadPath = Paths.get(baseUploadDir, category).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        //Save file with new name
        Path filePath = uploadPath.resolve(newFileName);
        file.transferTo(filePath.toFile());
        savedImage.setFileName(newFileName);
        savedImage.setFilePath("/uploads/" + category + "/" + newFileName);

        return imageRepository.save(savedImage);
    }

    //Delete image
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

