package com.dcava.dcava_backend.dto;

import com.dcava.dcava_backend.model.SaleItem;

public class SaleItemDTO {
    private Integer productId;
    private String itemName;
    private String itemDescription;
    private boolean external;
    private int quantity;
    private double unitPrice;
    private double unitCost;

    public SaleItemDTO() {}

    public SaleItemDTO(SaleItem item) {
        this.productId = item.getProduct() != null ? item.getProduct().getId() : null;
        this.itemName = item.getItemName();
        this.itemDescription = item.getItemDescription();
        this.external = item.isExternal();
        this.quantity = item.getQuantity();
        this.unitPrice = item.getUnitPrice();
        this.unitCost = item.getUnitCost();
    }

    // getters & setters
    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
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

}


