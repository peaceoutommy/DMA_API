package dev.tomas.dma.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompanyRoleCreateReq {
    @Positive(message = "Company Id must be positive")
    private Integer companyId;
    @NotBlank
    @Size(min = 1, max = 100, message = "Please provide a company role name between 1 and 50 characters")
    private String name;
}
