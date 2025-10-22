package dev.tomas.dma.dto.response;

import jakarta.annotation.Nullable;
import lombok.Data;

@Data
public class AuthUserRes {
    public Integer id;
    public String username;
    public String email;
    public String firstName;
    public String lastName;
    @Nullable
    public Integer companyId;
    @Nullable
    public String role;
}
