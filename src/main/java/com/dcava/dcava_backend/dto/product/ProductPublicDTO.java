package com.dcava.dcava_backend.dto.product;

import com.dcava.dcava_backend.model.Product;
import java.time.LocalDateTime;

public class ProductPublicDTO {
    private int id;
    private String name;
    private String description;
    private double price;
    private String category;
    private String status;
    private int stock;
    private String compatibility;
    private LocalDateTime createdAt;

    public ProductPublicDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.category = product.getCategory();
        this.status = product.getStatus();
        this.stock = product.getStock();
        this.compatibility = product.getCompatibility();
        this.createdAt = product.getCreatedAt();
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getStatus() {
        return status;
    }

    public int getStock() {
        return stock;
    }

    public String getCompatibility() {
        return compatibility;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
