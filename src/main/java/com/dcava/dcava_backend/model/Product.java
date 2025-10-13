package com.dcava.dcava_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name_product", nullable = false)
    private String name;

    @Column(name = "description_product", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private double price;

    //@JsonIgnore
    @Column(nullable = false)
    private double cost;

    @Column(length = 64)
    private String category;

    @Column(name = "status_product", length = 12, nullable = false)
    private String status = "active"; // o "INACTIVE" para borrado lógico

    @Column(nullable = false)
    private int stock;

    // use hashtags (#shimano, #mtb, etc.)
    @Column(name = "compatible_tags", columnDefinition = "TEXT")
    private String compatibility;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    //Getter and Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getCompatibility() { return compatibility; }
    public void setCompatibility(String compatibility) { this.compatibility = compatibility; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt;}
}