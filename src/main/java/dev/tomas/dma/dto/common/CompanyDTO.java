package dev.tomas.dma.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CompanyDTO {
    private Integer id;
    private String name;
    private String registrationNumber;
    private String taxId;
    private CompanyTypeDTO type;
    private String status;
}
