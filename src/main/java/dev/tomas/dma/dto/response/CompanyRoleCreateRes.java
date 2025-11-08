package dev.tomas.dma.dto.response;

import dev.tomas.dma.dto.common.CompanyPermissionDTO;
import lombok.Data;

import java.util.List;

@Data
public class CompanyRoleCreateRes {
    private Integer id;
    private Integer companyId;
    private String name;
    private List<CompanyPermissionDTO> permissions;
}
