package com.dcava.dcava_backend.repository;

import com.dcava.dcava_backend.model.UserAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAdminRepository extends JpaRepository<UserAdmin, Long> {
    Optional<UserAdmin> findByUidFirebase(String uidFirebase);
}
