package com.dcava.dcava_backend.controller;

import com.dcava.dcava_backend.model.Advertisement;
import com.dcava.dcava_backend.model.Advertisement.AdType;
import com.dcava.dcava_backend.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AdvertisementController {

    @Autowired
    private AdvertisementService advertisementService;

    // Public Endpoints
    @GetMapping("/advertisements")
    public ResponseEntity<List<Advertisement>> getAllAdvertisements() {
        return ResponseEntity.ok(advertisementService.getAllAdvertisements());
    }

    @GetMapping("/advertisements/type/{adType}")
    public ResponseEntity<List<Advertisement>> getAdvertisementsByType(@PathVariable String adType) {
        try {
            AdType type = AdType.valueOf(adType.toUpperCase());
            return ResponseEntity.ok(advertisementService.getAdvertisementsByType(type));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/advertisements/{id}")
    public ResponseEntity<Advertisement> getAdvertisementById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(advertisementService.getAdvertisementById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/advertisements/types")
    public ResponseEntity<Map<String, Object>> getAdTypes() {
        Map<String, Object> response = new HashMap<>();
        Map<String, Map<String, String>> types = new HashMap<>();

        types.put("BANNER", Map.of(
                "description", "Top banner",
                "dimensions", "1024px minimum width x 200-800px height",
                "aspectRatio", "~4:1"
        ));
        types.put("SIDEBAR", Map.of(
                "description", "Side ad",
                "dimensions", "1200px width x 1000-3000px height",
                "aspectRatio", "~1:2"
        ));
        types.put("SQUARE", Map.of(
                "description", "Square ad",
                "dimensions", "600-2000px x 600-2000px",
                "aspectRatio", "1:1"
        ));
        types.put("POPUP", Map.of(
                "description", "Popup/modal ad",
                "dimensions", "600-800px width x 400-600px height",
                "aspectRatio", "~4:3"
        ));

        response.put("types", types);
        return ResponseEntity.ok(response);
    }

    // Private endpoints

    @PostMapping("/admin/advertisements/upload")
    public ResponseEntity<Map<String, Object>> uploadAdvertisement(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("adType") String adTypeStr
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            // validate file
            if (file.isEmpty()) {
                response.put("success", false);
                response.put("message", "file it's empty");
                return ResponseEntity.badRequest().body(response);
            }

            // validate content
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "The file must be an image");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate max size (max 8MB)
            if (file.getSize() > 8 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "The file must not exceed 5MB");
                return ResponseEntity.badRequest().body(response);
            }

            // convert and validate add
            AdType adType;
            try {
                adType = AdType.valueOf(adTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                response.put("success", false);
                response.put("message", "Invalid ad type: " + adTypeStr);
                return ResponseEntity.badRequest().body(response);
            }

            // Create add
            Advertisement advertisement = advertisementService.createAdvertisement(file, title, adType);

            response.put("success", true);
            response.put("message", "Ad created successfully");
            response.put("advertisement", advertisement);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Error saving file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @DeleteMapping("/admin/advertisements/{id}")
    public ResponseEntity<Map<String, Object>> deleteAdvertisement(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();

        try {
            advertisementService.deleteAdvertisement(id);
            response.put("success", true);
            response.put("message", "Ad successfully removed");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}