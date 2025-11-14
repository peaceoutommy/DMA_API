package dev.tomas.dma.dto.request;


import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthReq {
    private String username;
    private String email;
    @NotBlank
    private String password;

    @AssertTrue(message = "Please provide an username or email")
    public boolean isUsernameOrEmailProvided() {
        return (username != null && !username.isBlank()) || (email != null && !email.isBlank());
    }
}
