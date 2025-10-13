package com.dcava.dcava_backend.service;


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
    public Sale createSale(List<SaleItemDTO> itemsDTO, UserAdmin user) {
        Sale sale = new Sale();
        sale.setUser(user);
        sale.setSaleDate(LocalDateTime.now());

        double total = 0.0;
        List<SaleItem> items = new ArrayList<>();

        for (SaleItemDTO dto : itemsDTO) {
            Product product = productRepository.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found " + dto.getProductId()));

            if (product.getStock() < dto.getQuantity()) {
                throw new RuntimeException("insufficient stock " + product.getName());
            }

            if(dto.getQuantity() < 1){
                throw new RuntimeException("insufficient quantity from" + product.getName());
            }

            if(Objects.equals(product.getStatus(), "inactive")) {
                throw new RuntimeException("Product not found " + dto.getProductId());
            }

            // Discount stock
            product.setStock(product.getStock() - dto.getQuantity());
            productRepository.save(product);

            double price = product.getPrice();
            double cost = product.getCost();
            double profit = (price - cost) * dto.getQuantity();

            SaleItem item = new SaleItem();
            item.setProduct(product);
            item.setQuantity(dto.getQuantity());
            item.setUnitPrice(price);
            item.setUnitCost(cost);
            item.setProfit(profit);
            item.setSale(sale);
            items.add(item);

            total += price * dto.getQuantity();
        }

        sale.setItems(items);
        sale.setTotal(total);

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

