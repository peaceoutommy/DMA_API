package dev.tomas.dma.dto.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CompanyTypeDTO {
    @Positive
    private Integer id;
    @NotBlank
    @Size(min = 3, max = 100, message = "Company type be between 3 and 100 characters")
    private String name;
    @Size(max = 500)
    private String description;
}
