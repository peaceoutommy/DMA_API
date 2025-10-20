package dev.tomas.dma.dto.response;

import lombok.Data;

@Data
public class AuthUserRes {
    public Integer id;
    public String username;
    public String email;
    public String firstName;
    public String lastName;
}
