package com.dcava.dcava_backend.repository;


import com.dcava.dcava_backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Search for (name, description or compatibility)
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(p.compatibility) LIKE LOWER(CONCAT('%', :text, '%'))")
    Page<Product> searchProducts(String text, Pageable pageable);
}
