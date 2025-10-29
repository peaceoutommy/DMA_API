package dev.tomas.dma.dto.response;

import lombok.Data;

@Data
public class CompanyPermissionCreateRes {
    private Integer id;
    private String name;
    private String description;
    private String type;
}
