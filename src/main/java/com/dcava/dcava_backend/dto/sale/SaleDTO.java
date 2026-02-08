package com.dcava.dcava_backend.dto.sale;

import com.dcava.dcava_backend.dto.user.UserAdminDTO;
import com.dcava.dcava_backend.model.Sale;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SaleDTO {

    private Integer id;
    private LocalDateTime saleDate;

    private double subtotal;
    private double discount;
    private double total;

    private String notes;

    private UserAdminDTO user;
    private List<SaleItemDTO> items;

    public SaleDTO(Sale sale) {
        this.id = sale.getId();
        this.saleDate = sale.getSaleDate();
        this.subtotal = sale.getSubtotal();
        this.discount = sale.getDiscount();
        this.total = sale.getTotal();
        this.notes = sale.getNotes();

        if (sale.getUser() != null) {
            this.user = new UserAdminDTO(sale.getUser());
        }

        this.items = sale.getItems().stream()
                .map(SaleItemDTO::new)
                .collect(Collectors.toList());
    }

    // getters & setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public UserAdminDTO getUser() {
        return user;
    }

    public void setUser(UserAdminDTO user) {
        this.user = user;
    }

    public List<SaleItemDTO> getItems() {
        return items;
    }

    public void setItems(List<SaleItemDTO> items) {
        this.items = items;
    }
}