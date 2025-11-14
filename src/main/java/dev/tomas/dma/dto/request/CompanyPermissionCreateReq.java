package dev.tomas.dma.dto.request;

import lombok.Data;

@Data
public class CompanyPermissionCreateReq {
    private String name;
    private String description;
    private String type;
}
