package dev.tomas.dma.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompanyTypeCreateReq {
    @NotBlank
    @Size(min = 1, max = 50, message = "Please provide a company type between 1 and 50 characters")
    public String name;
}
