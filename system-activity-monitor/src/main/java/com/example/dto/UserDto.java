package com.example.dto;

import java.time.LocalDateTime;

public class UserDto {
    private Integer id;
    private String username;
    private String email;
    private LocalDateTime createdAt;

    public UserDto(Integer id, String username, String email, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
