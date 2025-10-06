package dev.tomas.dma.dto;

import lombok.Data;

@Data
public class AuthUserResponse {
    public Integer id;
    public String username;
    public String email;
    public String firstName;
    public String lastName;
}
