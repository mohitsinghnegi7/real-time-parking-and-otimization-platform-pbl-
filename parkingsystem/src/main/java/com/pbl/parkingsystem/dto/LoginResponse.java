package com.pbl.parkingsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private String message;
    private Long id;
    private String name;
    private String role;
    private String token;
}
