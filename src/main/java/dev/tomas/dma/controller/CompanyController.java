package dev.tomas.dma.controller;

import dev.tomas.dma.dto.CompanyTypeCreateRequestDto;
import dev.tomas.dma.dto.CompanyTypeCreateResponseDto;
import dev.tomas.dma.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService companyService;

    @PostMapping("type")
    public CompanyTypeCreateResponseDto createType(@Valid @RequestBody CompanyTypeCreateRequestDto request) {
        return companyService.saveType(request);
    }
}
