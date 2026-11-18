package com.pbl.parkingsystem.dto;

public class LoginResponse {
    private String message;
    private Long id;
    private String name;
    private String role;
    private String token;

    public String getMessage() {
        return message;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}
