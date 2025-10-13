package com.dcava.dcava_backend.dto;

import com.dcava.dcava_backend.model.Sale;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SaleDTO {
    private Integer id;
    private LocalDateTime saleDate;
    private double total;
    private UserSummaryDTO user;
    private List<SaleItemDTO> items;

    public SaleDTO(Sale sale) {
        this.id = sale.getId();
        this.saleDate = sale.getSaleDate();
        this.total = sale.getTotal();
        if (sale.getUser() != null) {
            this.user = new UserSummaryDTO(sale.getUser());
        }
        this.items = sale.getItems().stream()
                .map(SaleItemDTO::new)
                .collect(Collectors.toList());
    }

    //Getter and Setter
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

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public UserSummaryDTO getUser() {
        return user;
    }

    public void setUser(UserSummaryDTO user) {
        this.user = user;
    }

    public List<SaleItemDTO> getItems() {
        return items;
    }

    public void setItems(List<SaleItemDTO> items) {
        this.items = items;
    }
}
