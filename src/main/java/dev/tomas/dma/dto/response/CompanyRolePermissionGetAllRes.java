package dev.tomas.dma.dto.response;

import dev.tomas.dma.dto.common.CompanyRolePermissionDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyRolePermissionGetAllRes {
    private List<CompanyRolePermissionDTO> permissions = new ArrayList<>();
}
