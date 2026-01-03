package com.dcava.dcava_backend.dto;

import com.dcava.dcava_backend.model.Sale;
import com.dcava.dcava_backend.model.SaleItem;

public class SaleDetailDTO extends SaleDTO {

    private double cost;
    private double profit;

    public SaleDetailDTO(Sale sale) {
        super(sale);

        double totalCost = 0.0;

        for (SaleItem item : sale.getItems()) {
            totalCost += item.getUnitCost() * item.getQuantity();
        }

        this.cost = totalCost;
        this.profit = sale.getTotal() - totalCost;
    }

    public double getCost() {
        return cost;
    }

    public double getProfit() {
        return profit;
    }
}
