package dev.tomas.dma.controller;

import dev.tomas.dma.dto.*;
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
    public Optional<CompanyTypeGetAllRes> getAllTypes(){
        return companyService.getAllTypes();
    }

    @GetMapping("types/{id}")
    public Optional<CompanyTypeGetRes> getTypeById(@PathVariable Integer id){
        return companyService.getTypeById(id);
    }

    @PostMapping("types")
    public CompanyTypeGetRes createType(@Valid @RequestBody CompanyTypeCreateReq request) {
        return companyService.saveType(request);
    }

    // Endpoint for creating Company
    @PostMapping
    public CompanyCreateRes createCompany(@Valid @RequestBody CompanyCreateReq request) {
        return companyService.saveCompany(request);
    }

}
