package com.capstone.OpportuGrow.Dto;

import java.time.LocalDateTime;
import java.util.Date;

public record UserTableDto (
        Integer id,
        String name,
        String email,
        String role,
        boolean active,
        Date creation
    ) {
    @Override
    public Integer id() {
        return id;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String email() {
        return email;
    }

    @Override
    public String role() {
        return role;
    }

    @Override
    public boolean active() {
        return active;
    }

    public Date getCreation() {
        return creation;
    }
}


