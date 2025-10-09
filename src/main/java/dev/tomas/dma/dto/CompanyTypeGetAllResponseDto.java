package dev.tomas.dma.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CompanyTypeGetAllResponseDto {
    public List<CompanyTypeDto> types = new ArrayList<>();
}
