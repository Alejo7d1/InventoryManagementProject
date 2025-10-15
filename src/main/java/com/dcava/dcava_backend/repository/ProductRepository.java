package com.dcava.dcava_backend.repository;


import com.dcava.dcava_backend.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Search for (name, description or compatibility)
    @Query("SELECT p FROM Product p WHERE p.status = 'active' AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :text, '%')) OR " +
            "LOWER(p.compatibility) LIKE LOWER(CONCAT('%', :text, '%')))")
    Page<Product> searchProducts(@Param("text") String text, Pageable pageable);

    //List of product in the category
    @Query("SELECT DISTINCT p.category FROM Product p " + "WHERE p.status = 'active' AND p.category IS NOT NULL AND p.category <> ''")
    List<String> findDistinctCategories();

    @Query("SELECT p FROM Product p WHERE LOWER(p.category) = LOWER(:category) AND p.status = 'active'")
    List<Product> findByCategoryIgnoreCase(@Param("category") String category);

    @Query("SELECT p FROM Product p WHERE p.status = 'inactive'")
    List<Product> findDeletedProducts();
}
