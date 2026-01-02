package com.dcava.dcava_backend.service;


import com.dcava.dcava_backend.dto.CreateSaleDTO;
import com.dcava.dcava_backend.model.*;
import com.dcava.dcava_backend.repository.ProductRepository;
import com.dcava.dcava_backend.repository.SaleRepository;
import com.dcava.dcava_backend.dto.SaleItemDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Sale createSale(CreateSaleDTO request, UserAdmin user) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Sale must contain at least one item");
        }

        Sale sale = new Sale();
        sale.setUser(user);
        sale.setSaleDate(LocalDateTime.now());

        double subtotal = 0.0;
        List<SaleItem> items = new ArrayList<>();

        for (SaleItemDTO dto : request.getItems()) {

            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found " + dto.getProductId()));

            if ("inactive".equals(product.getStatus())) {
                throw new RuntimeException("Product inactive " + product.getName());
            }

            if (dto.getQuantity() < 1) {
                throw new RuntimeException("Invalid quantity for " + product.getName());
            }

            if (product.getStock() < dto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for " + product.getName());
            }

            // Update stock
            product.setStock(product.getStock() - dto.getQuantity());
            productRepository.save(product);

            double unitPrice = product.getPrice();
            double unitCost = product.getCost();
            double itemTotal = unitPrice * dto.getQuantity();

            SaleItem item = new SaleItem();
            item.setProduct(product);
            item.setQuantity(dto.getQuantity());
            item.setUnitPrice(unitPrice);
            item.setUnitCost(unitCost);
            item.setProfit((unitPrice - unitCost) * dto.getQuantity());
            item.setSale(sale);

            items.add(item);
            subtotal += itemTotal;
        }

        sale.setItems(items);
        sale.setSubtotal(subtotal);

        double total = subtotal;
        double discount = 0.0;

        if (request.getFinalTotal() != null) {

            double finalTotal = request.getFinalTotal();

            if (finalTotal <= 0) {
                throw new RuntimeException("Final total must be greater than zero");
            }

            if (finalTotal > subtotal) {
                throw new RuntimeException("Final total cannot exceed subtotal");
            }

            discount = subtotal - finalTotal;
            total = finalTotal;
        }

        sale.setDiscount(discount);
        sale.setTotal(total);
        sale.setNotes(request.getNotes());

        return saleRepository.save(sale);
    }


    public Optional<Sale> getById(Integer id) {
        return saleRepository.findById(id);
    }

    public List<Sale> getByDateRange(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findByDateRange(start, end);
    }

    public List<Sale> getByUserAndDateRange(Integer userId, LocalDateTime start, LocalDateTime end) {
        return saleRepository.findByUserAndDateRange(userId,start,end);
    }
}

