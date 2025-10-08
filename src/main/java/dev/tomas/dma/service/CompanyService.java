package dev.tomas.dma.service;

import dev.tomas.dma.dto.CompanyTypeCreateRequestDto;
import dev.tomas.dma.dto.CompanyTypeCreateResponseDto;

public interface CompanyService {
    CompanyTypeCreateResponseDto saveType(CompanyTypeCreateRequestDto request);

}
