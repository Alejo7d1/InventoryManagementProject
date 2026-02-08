package com.dcava.dcava_backend.repository;

import com.dcava.dcava_backend.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findByNameIgnoreCaseAndStatus(String name, String status);
    Optional<Category> findBySlugIgnoreCase(String slug);
    Optional<Category> findByNameIgnoreCase(String name);
    boolean existsBySlugIgnoreCase(String slug);
    boolean existsByNameIgnoreCase(String name);

    @Query("SELECT c FROM Category c " +
            "WHERE c.status = :status AND LOWER(c.name) IN :lowerNames")
    List<Category> findActiveByLowerNames(
            @Param("status") String status,
            @Param("lowerNames") Collection<String> lowerNames
    );

}
