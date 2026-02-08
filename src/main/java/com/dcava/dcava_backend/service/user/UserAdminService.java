package com.dcava.dcava_backend.service.user;

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

    public Optional<UserAdmin> findByUid(String uid) {
        return userRepo.findByUidFirebase(uid);
    }

    public boolean existsByUid(String uid) {
        return userRepo.existsByUidFirebase(uid);
    }

}
