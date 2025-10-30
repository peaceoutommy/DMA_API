package dev.tomas.dma.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompanyTypeGetRes {
    public Integer id;
    public String name;
    public String description;
}
