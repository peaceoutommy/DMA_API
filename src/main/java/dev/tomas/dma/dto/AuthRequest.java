package dev.tomas.dma.dto;


import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class AuthRequest {
    private String username;
    private String email;
    @NotBlank
    private String password;

    @AssertTrue(message = "Please provide an username or email")
    public boolean isUsernameOrEmailProvided() {
        return (username != null && !username.isBlank()) || (email != null && !email.isBlank());
    }
}
