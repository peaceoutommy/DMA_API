package dev.tomas.dma.dto;

import lombok.Data;

@Data
public class AuthResponse {
    public String token;
    public AuthUserResponse user;
}
