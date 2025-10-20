package dev.tomas.dma.dto.response;

import dev.tomas.dma.dto.common.CompanyTypeDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CompanyCreateRes {
    private Integer id;
    private String name;
    private String registrationNumber;
    private String taxId;
    private CompanyTypeDTO type;
}
