package dev.tomas.dma.dto;

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
