package dev.tomas.dma.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRes {
    public String token;
    public AuthUserResponse user;
}
