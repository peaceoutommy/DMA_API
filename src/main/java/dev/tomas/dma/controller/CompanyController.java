package dev.tomas.dma.controller;

import dev.tomas.dma.dto.common.CompanyDTO;
import dev.tomas.dma.dto.common.CompanyTypeDTO;
import dev.tomas.dma.dto.common.FundRequestDTO;
import dev.tomas.dma.dto.common.UserDTO;
import dev.tomas.dma.dto.request.AddUserToCompanyReq;
import dev.tomas.dma.dto.request.CompanyCreateReq;
import dev.tomas.dma.dto.request.CompanyTypeCreateReq;
import dev.tomas.dma.dto.request.FundRequestCreateReq;
import dev.tomas.dma.dto.response.*;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.service.CompanyEmployeeService;
import dev.tomas.dma.service.CompanyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<CompanyGetAllRes> getAll() {
        return ResponseEntity.ok(companyService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDTO> getById(@PathVariable Integer id ){
        return ResponseEntity.ok(companyService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CompanyDTO> create(@Valid @RequestBody CompanyCreateReq request) {
        return ResponseEntity.ok(companyService.save(request));
    }

    @GetMapping("types")
    public ResponseEntity<CompanyTypeGetAllRes> getAllTypes() {
        return ResponseEntity.ok(companyService.getAllTypes());
    }

    @GetMapping("types/{id}")
    public ResponseEntity<CompanyTypeGetRes> getTypeById(@PathVariable Integer id) {
        return ResponseEntity.ok(companyService.getTypeById(id));
    }

    @PostMapping("types")
    public ResponseEntity<CompanyTypeGetRes> createType(@Valid @RequestBody CompanyTypeCreateReq request) {
        return ResponseEntity.ok(companyService.saveType(request));
    }

    @PutMapping("types")
    public ResponseEntity<CompanyTypeDTO> updateType(@Valid @RequestBody CompanyTypeDTO request) {
        return ResponseEntity.ok(companyService.updateType(request));
    }

    @DeleteMapping("types/{id}")
    public Integer deleteType(@Positive @PathVariable Integer id) {
        if (Objects.isNull(id)) {
            throw new IllegalArgumentException("Company type id cannot be null");
        }
        return companyService.deleteType(id);
    }

    @PreAuthorize("hasAuthority('PERMISSION_Submit funding')")
    @PostMapping("funding")
    public ResponseEntity<FundRequestDTO> submitFundingRequest(@RequestBody FundRequestCreateReq req){
        return ResponseEntity.ok(companyService.submitFundRequest(req));
    }
}
