package dev.tomas.dma.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class User {
    private Integer id;

    @NotNull
    @Email(message = "Please provide a valid email")
    private String email;

    @NotNull
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain upper, lower, number, and special character"
    )
    private String password;

    @NotNull
    private String phoneNumber;

    @NotNull
    private String address;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private String middleNames;

    @NotNull
    private String username;
}
