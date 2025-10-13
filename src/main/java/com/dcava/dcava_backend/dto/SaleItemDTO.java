package com.dcava.dcava_backend.dto;

import com.dcava.dcava_backend.model.SaleItem;

public class SaleItemDTO {
    private Integer productId;
    private String productName;
    private int quantity;
    private double unitPrice;
    private double unitCost;
    private double profit;

    //important, used by jackson he he
    public SaleItemDTO() {}

    public SaleItemDTO(SaleItem item) {
        this.productId = item.getProduct().getId();
        this.productName = item.getProduct().getName();
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnitPrice();
        this.unitCost = item.getUnitCost();
        this.profit = item.getProfit();
    }

    // getters and Setters
    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(double unitCost) {
        this.unitCost = unitCost;
    }

    public double getProfit() {
        return profit;
    }

    public void setProfit(double profit) {
        this.profit = profit;
    }
}

