package dev.tomas.dma.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompanyCreateReq {
    @NotNull
    @Positive(message = "UserId must be defined")
    private Integer userId;
    @NotBlank
    @Size(min = 3, max = 100, message = "Company name must be at least 3 characters")
    private String name;
    @NotBlank
    @Size(min = 5, max = 20, message = "Registration number must be at least 5 characters")
    private String registrationNumber;
    @NotBlank
    @Size(min = 8, max = 30, message = "Tax number must be at least 8 characters")
    private String taxId;
    @NotNull
    @Positive
    private Integer typeId;
}
