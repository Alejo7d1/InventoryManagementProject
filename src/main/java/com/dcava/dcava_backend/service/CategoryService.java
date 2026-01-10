package com.dcava.dcava_backend.service;

import com.dcava.dcava_backend.dto.ProductPublicDTO;
import com.dcava.dcava_backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final ProductRepository productRepo;

    public CategoryService(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    public List<String> getAllCategories() {
        return productRepo.findDistinctCategories();
    }

    public List<ProductPublicDTO> getProductsByCategory(String category) {
        return productRepo.findByCategoryIgnoreCase(category)
                .stream()
                .map(ProductPublicDTO::new)
                .toList();
    }
}

