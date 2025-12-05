package com.dcava.dcava_backend.service;

import com.dcava.dcava_backend.model.Product;
import com.dcava.dcava_backend.model.ProductImage;
import com.dcava.dcava_backend.repository.ProductImageRepository;
import com.dcava.dcava_backend.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepo;
    private final ProductImageService imageService;
    private final ProductImageRepository productImageRepository;

    public ProductService(ProductRepository productRepo, ProductImageService imageService, ProductImageRepository productImageRepository) {
        this.productRepo = productRepo;
        this.imageService = imageService;
        this.productImageRepository = productImageRepository;
    }

    public Page<Product> search(String text, int page, String sort, String order) {
        List<String> allowedSorts = List.of("id", "stock", "price", "name");
        if (!allowedSorts.contains(sort)) {
            sort = "id"; // fallback
        }

        //Sort direction
        Sort.Direction direction = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sortObj = Sort.by(direction, sort);

        return productRepo.searchProducts(text, PageRequest.of(page, 10, sortObj));
    }

    public Page<Product> adminSearch(String text, int page, String status) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("id").ascending());
        String normalizedStatus = (status == null || status.isBlank()) ? null : status.toLowerCase();
        return productRepo.searchAdminProducts(text, normalizedStatus, pageable);
    }



    public Optional<Product> findById(int id) {
        return productRepo.findById(id);
    }

    @Transactional
    public Product save(Product product, List<MultipartFile> images) throws IOException {
        Product savedProduct = productRepo.save(product);

        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                imageService.saveImage(savedProduct.getId(), file);
            }
        } else {
            imageService.saveGenericImage(savedProduct.getId());
        }

        return savedProduct;
    }

    public Product update(int id, Product updated) {
        return productRepo.findById(id).map(p -> {
            p.setName(updated.getName());
            p.setDescription(updated.getDescription());
            p.setPrice(updated.getPrice());
            p.setCost(updated.getCost());
            p.setStock(updated.getStock());
            p.setCompatibility(updated.getCompatibility());
            p.setCategory(updated.getCategory());

            return productRepo.save(p);
        }).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public boolean restore(int id) {
        return productRepo.findById(id).map(p -> {
            p.setStatus("active");
            productRepo.save(p);
            return true;
        }).orElse(false);
    }

    //delete product
    @Transactional
    public boolean deactivate(int id) {
        return productRepo.findById(id).map(product -> {

            // Get all product images
            List<ProductImage> images = productImageRepository.findByProductId(id);

            // Remove all non-default images
            for (ProductImage img : images) {
                if (!"default.png".equals(img.getFileName())) {
                    imageService.deleteImage(img.getId());
                }
            }
            boolean hasDefault = productImageRepository.findByProductId(id).stream()
                    .anyMatch(img -> "default.png".equals(img.getFileName()));

            if (!hasDefault) imageService.saveGenericImage(product.getId());

            product.setStatus("inactive");
            productRepo.save(product);
            return true;
        }).orElse(false);
    }

    public boolean updateStock(int id, int quantity) {
        return productRepo.findById(id).map(p -> {
            p.setStock(quantity);
            productRepo.save(p);
            return true;
        }).orElse(false);
    }

    public List<Product> getAllProductsDeactivated() {
        return productRepo.findDeletedProducts();
    }
}

