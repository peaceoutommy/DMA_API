package dev.tomas.dma.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyRolePermissionDTO {
    private Integer id;
    private String name;
    private String type;
    private String description;
}
