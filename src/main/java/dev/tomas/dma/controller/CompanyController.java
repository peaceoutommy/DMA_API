package dev.tomas.dma.controller;

import dev.tomas.dma.dto.CompanyTypeCreateRequestDto;
import dev.tomas.dma.dto.CompanyTypeDto;
import dev.tomas.dma.dto.CompanyTypeGetAllResponseDto;
import dev.tomas.dma.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping("types")
    public Optional<CompanyTypeGetAllResponseDto> getAllTypes(){
        return companyService.getAllTypes();
    }

    @GetMapping("types/{id}")
    public Optional<CompanyTypeDto> getTypeById(@PathVariable Integer id){
        return companyService.getTypeById(id);
    }

    @PostMapping("types")
    public CompanyTypeDto createType(@Valid @RequestBody CompanyTypeCreateRequestDto request) {
        return companyService.saveType(request);
    }



}
