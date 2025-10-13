package com.dcava.dcava_backend.repository;

import com.dcava.dcava_backend.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Integer> {

    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :start AND :end")
    List<Sale> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT s FROM Sale s WHERE s.user.id = :userId AND s.saleDate BETWEEN :start AND :end")
    List<Sale> findByUserAndDateRange(@Param("userId") Integer userId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

}

