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

    public ProductService(ProductRepository productRepo, ProductImageService imageService) {
        this.productRepo = productRepo;
        this.imageService = imageService;
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

    public boolean deactivate(int id) {
        return productRepo.findById(id).map(p -> {
            p.setStatus("inactive");
            productRepo.save(p);
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

    public List<Product> getAllProductsDesactivated() {
        return productRepo.findDeletedProducts();
    }
}

