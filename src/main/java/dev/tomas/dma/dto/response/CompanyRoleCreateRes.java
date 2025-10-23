package dev.tomas.dma.dto.response;

import lombok.Data;

@Data
public class CompanyRoleCreateRes {
    private Integer id;
    private Integer companyId;
    private String name;
}
