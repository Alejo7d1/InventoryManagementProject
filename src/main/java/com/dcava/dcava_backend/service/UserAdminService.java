package com.dcava.dcava_backend.service;

import com.dcava.dcava_backend.model.UserAdmin;
import com.dcava.dcava_backend.repository.UserAdminRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserAdminService {

    private final UserAdminRepository userRepo;

    public UserAdminService(UserAdminRepository userRepo) {
        this.userRepo = userRepo;
    }

    // search user by id
    public Optional<UserAdmin> findByUid(String uid) {
        return userRepo.findByUidFirebase(uid);
    }

    // Create user if it doesn't exist
    public UserAdmin registerIfNotExists(String uid, String name, String email) {
        return userRepo.findByUidFirebase(uid)
                .orElseGet(() -> userRepo.save(new UserAdmin(name, email, uid)));
    }

}
