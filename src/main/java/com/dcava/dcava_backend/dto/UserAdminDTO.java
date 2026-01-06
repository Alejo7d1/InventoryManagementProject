package com.dcava.dcava_backend.dto;

import com.dcava.dcava_backend.model.UserAdmin;

public class UserAdminDTO {
    private Integer id;
    private String name;
    private String email;

    public UserAdminDTO(UserAdmin user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
    }

    // getters
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}

