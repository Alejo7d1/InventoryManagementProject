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
    @Query("""
SELECT p FROM Product p
    WHERE p.status = 'active'
      AND (
        LOWER(p.name) LIKE LOWER(CONCAT('%', :text, '%'))
        OR LOWER(p.description) LIKE LOWER(CONCAT('%', :text, '%'))
        OR LOWER(p.compatibility) LIKE LOWER(CONCAT('%', :text, '%'))
      )
      AND (:category IS NULL OR LOWER(p.category) = LOWER(:category))
""")
    Page<Product> searchProducts(
            @Param("text") String text,
            @Param("category") String category,
            Pageable pageable
    );

    //List all
    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(p.compatibility) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:status IS NULL OR p.status = :status)")
    Page<Product> searchAdminProducts(@Param("text") String text,
                                      @Param("status") String status,
                                      Pageable pageable);

    //List of product in the category
    @Query("SELECT DISTINCT p.category FROM Product p " + "WHERE p.status = 'active' AND p.category IS NOT NULL AND p.category <> ''")
    List<String> findDistinctCategories();

    @Query("SELECT p FROM Product p WHERE LOWER(p.category) = LOWER(:category) AND p.status = 'active'")
    List<Product> findByCategoryIgnoreCase(@Param("category") String category);

    @Query("SELECT p FROM Product p WHERE p.status = 'inactive'")
    List<Product> findDeletedProducts();

    //Show low stock
    @Query("""
        SELECT p FROM Product p WHERE p.stock <= :threshold AND p.status = 'active' ORDER BY p.stock ASC""")
    Page<Product> findLowStockProducts(
        @Param("threshold") int threshold,
        Pageable pageable
    );
}
