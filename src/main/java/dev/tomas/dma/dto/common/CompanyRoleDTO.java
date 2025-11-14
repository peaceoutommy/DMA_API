package dev.tomas.dma.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CompanyRoleDTO {
    private Integer id;
    private String name;
    private Integer companyId;
    private List<CompanyPermissionDTO> permissions = new ArrayList<>();
}
