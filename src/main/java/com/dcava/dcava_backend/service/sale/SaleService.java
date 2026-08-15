package com.dcava.dcava_backend.service.sale;


import com.dcava.dcava_backend.dto.sale.CreateSaleDTO;
import com.dcava.dcava_backend.model.*;
import com.dcava.dcava_backend.repository.ProductRepository;
import com.dcava.dcava_backend.repository.SaleRepository;
import com.dcava.dcava_backend.dto.sale.SaleItemDTO;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SaleService {

    private static final Logger log = LoggerFactory.getLogger(SaleService.class);

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Sale createSale(CreateSaleDTO request, UserAdmin user) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            log.warn("Sale rejected: empty items list, userId={}", user != null ? user.getId() : null);
            throw new RuntimeException("Sale must contain at least one item");
        }

        Sale sale = new Sale();
        sale.setUser(user);
        sale.setSaleDate(LocalDateTime.now());
        sale.setNotes(request.getNotes());

        double subtotal = 0.0;
        List<SaleItem> items = new ArrayList<>();

        for (SaleItemDTO dto : request.getItems()) {

            if (dto.getQuantity() < 1) {
                throw new RuntimeException("Invalid quantity");
            }

            SaleItem item = new SaleItem();
            item.setSale(sale);
            item.setQuantity(dto.getQuantity());

            // If catalogued product
            if (dto.getProductId() != null) {

                Product product = productRepository.findById(dto.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found " + dto.getProductId()));

                if ("inactive".equals(product.getStatus())) {
                    log.warn("Sale rejected: inactive product, productId={} name={}",
                            product.getId(), product.getName());
                    throw new RuntimeException("Product inactive " + product.getName());
                }

                if (product.getStock() < dto.getQuantity()) {
                    log.warn("Sale rejected: insufficient stock, productId={} name={} requested={} stock={}",
                            product.getId(), product.getName(), dto.getQuantity(), product.getStock());
                    throw new RuntimeException("Insufficient stock for " + product.getName());
                }

                product.setStock(product.getStock() - dto.getQuantity());
                productRepository.save(product);

                item.setProduct(product);
                item.setItemName(product.getName());
                item.setItemDescription(product.getDescription());
                item.setUnitPrice(product.getPrice());
                item.setUnitCost(product.getCost());
                item.setExternal(false);

            }
            // If external Product
            else {

                if (dto.getItemName() == null || dto.getItemName().isBlank()) {
                    throw new RuntimeException("External item must have a name");
                }

                if (dto.getUnitPrice() <= 0) {
                    throw new RuntimeException("Invalid unit price for external item");
                }

                item.setProduct(null);
                item.setItemName(dto.getItemName());
                item.setItemDescription(dto.getItemDescription());
                item.setUnitPrice(dto.getUnitPrice());
                item.setUnitCost(dto.getUnitCost());
                item.setExternal(true);
            }

            double itemTotal = item.getUnitPrice() * item.getQuantity();

            subtotal += itemTotal;
            items.add(item);
        }

        sale.setItems(items);
        sale.setSubtotal(subtotal);

        // discount
        double discount = 0.0;
        double total = subtotal;

        if (request.getFinalTotal() != null) {

            if (request.getFinalTotal() <= 0) {
                throw new RuntimeException("Final total must be greater than zero");
            }

            if (request.getFinalTotal() > subtotal) {
                throw new RuntimeException("Final total cannot exceed subtotal");
            }

            discount = subtotal - request.getFinalTotal();
            total = request.getFinalTotal();
        }

        sale.setDiscount(discount);
        sale.setTotal(total);

        Sale saved = saleRepository.save(sale);
        log.info("Sale created: saleId={} userId={} total={} items={}",
                saved.getId(),
                user != null ? user.getId() : null,
                saved.getTotal(),
                items.size());
        return saved;
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
