package com.dcava.dcava_backend.controller;

import com.dcava.dcava_backend.dto.SaleDTO;
import com.dcava.dcava_backend.model.Sale;
import com.dcava.dcava_backend.service.SaleService;
import com.dcava.dcava_backend.service.UserAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserAdminController {

    private final UserAdminService userService;
    private final SaleService saleService;

    public UserAdminController(UserAdminService userService, SaleService saleService) {
        this.userService = userService;
        this.saleService = saleService;
    }

    //GET register user
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        String uid = authentication.getName();
        return userService.findByUid(uid)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    //GET sales by user
    @GetMapping("/{userId}/sales")
    public ResponseEntity<List<SaleDTO>> getUserSales(
            @PathVariable Integer userId,
            @RequestParam("start_date") LocalDateTime start,
            @RequestParam("end_date") LocalDateTime end) {

        List<Sale> sales = saleService.getByUserAndDateRange(userId,start,end);
        List<SaleDTO> dtos = sales.stream().map(SaleDTO::new).toList();
        return ResponseEntity.ok(dtos);
    }
}

