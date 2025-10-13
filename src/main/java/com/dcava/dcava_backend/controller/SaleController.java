package com.dcava.dcava_backend.controller;

import com.dcava.dcava_backend.dto.SaleDTO;
import com.dcava.dcava_backend.model.Sale;
import com.dcava.dcava_backend.model.SaleItem;
import com.dcava.dcava_backend.model.UserAdmin;

import com.dcava.dcava_backend.service.SaleService;
import com.dcava.dcava_backend.service.UserAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//ALL Restricted
@RestController
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;
    private final UserAdminService userAdminService;

    @Autowired
    public SaleController(SaleService saleService, UserAdminService userAdminService) {
        this.saleService = saleService;
        this.userAdminService = userAdminService;
    }

    //Get sale by id
    @GetMapping("/{id}")
    public ResponseEntity<?> getSale(@PathVariable Integer id) {
        return saleService.getById(id)
                .<ResponseEntity<?>>map(sale -> ResponseEntity.ok(new SaleDTO(sale)))
                .orElse(ResponseEntity.status(404).body("Venta no encontrada"));
    }

    //Get sales by range
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSalesByDateRange(
            @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<Sale> sales = saleService.getByDateRange(start, end);
        List<SaleDTO> dtos = sales.stream().map(SaleDTO::new).toList();

        double totalSales = 0;
        double totalCost = 0;
        double totalProfit = 0;

        for (Sale sale : sales) {
            totalSales += sale.getTotal();
            for (SaleItem item : sale.getItems()) {
                totalCost += item.getUnitCost() * item.getQuantity();
                totalProfit += item.getProfit();
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("sales", dtos);
        response.put("totalSales", totalSales);
        response.put("totalCost", totalCost);
        response.put("totalProfit", totalProfit);

        return ResponseEntity.ok(response);
    }

    //Make a sale
    @PostMapping
    public ResponseEntity<?> createSale(
            Authentication authentication,
            @RequestBody List<com.dcava.dcava_backend.dto.SaleItemDTO> items) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(401).body("Usuario no autenticado");
            }

            String uid = authentication.getName();
            UserAdmin user = userAdminService.findByUid(uid)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            Sale sale = saleService.createSale(items, user);
            return ResponseEntity.ok(new SaleDTO(sale));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}


