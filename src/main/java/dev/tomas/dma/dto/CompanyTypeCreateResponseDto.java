package dev.tomas.dma.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompanyTypeCreateResponseDto {
    public Integer id;
    public String type;
}
