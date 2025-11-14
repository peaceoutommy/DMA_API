package dev.tomas.dma.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthRes {
    public String token;
    public AuthUserRes user;
}
