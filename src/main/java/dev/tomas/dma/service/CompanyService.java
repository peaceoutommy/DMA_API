package dev.tomas.dma.service;

import dev.tomas.dma.dto.CompanyTypeCreateRequestDto;
import dev.tomas.dma.dto.CompanyTypeDto;
import dev.tomas.dma.dto.CompanyTypeGetAllResponseDto;

import java.util.Optional;

public interface CompanyService {
    Optional <CompanyTypeGetAllResponseDto> getAllTypes();
    Optional<CompanyTypeDto> getTypeById(Integer id);
    CompanyTypeDto saveType(CompanyTypeCreateRequestDto request);

}
