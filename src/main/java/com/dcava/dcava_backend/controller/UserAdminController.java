package com.dcava.dcava_backend.controller;


import com.dcava.dcava_backend.model.UserAdmin;
import com.dcava.dcava_backend.service.UserAdminService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserAdminController {

    private final UserAdminService userService;

    public UserAdminController(UserAdminService userService) {
        this.userService = userService;
    }

    //POST /users/sync
    @PostMapping("/sync")
    public ResponseEntity<?> syncUser(@RequestHeader("Authorization") String authHeader,
                                      @RequestBody Map<String, String> body) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token);

            String uid = decoded.getUid();
            String name = body.getOrDefault("name", decoded.getName());
            String email = body.getOrDefault("email", decoded.getEmail());

            UserAdmin user = userService.registerIfNotExists(uid, name, email);
            return ResponseEntity.ok(user);

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(401).body("Invalid Firebase token");
        }
    }

    //GET /users/me
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }

        String uid = authentication.getName();
        return userService.findByUid(uid)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found")); //Puede dar problemas
    }
}

