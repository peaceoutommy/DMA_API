package dev.tomas.dma.controller;

import dev.tomas.dma.dto.common.CompanyDTO;
import dev.tomas.dma.dto.common.CompanyTypeDTO;
import dev.tomas.dma.dto.common.UserDTO;
import dev.tomas.dma.dto.request.AddUserToCompanyReq;
import dev.tomas.dma.dto.request.CompanyCreateReq;
import dev.tomas.dma.dto.request.CompanyTypeCreateReq;
import dev.tomas.dma.dto.response.*;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.service.CompanyEmployeeService;
import dev.tomas.dma.service.CompanyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final CompanyEmployeeService companyEmployeeService;

    @GetMapping
    public ResponseEntity<CompanyGetAllRes> getAll() {
        return companyService.getAll();
    }

    @PostMapping
    public ResponseEntity<CompanyDTO> create(@Valid @RequestBody CompanyCreateReq request) {
        return companyService.save(request);
    }

    @GetMapping("types")
    public ResponseEntity<CompanyTypeGetAllRes> getAllTypes() {
        return companyService.getAllTypes();
    }

    @GetMapping("types/{id}")
    public ResponseEntity<CompanyTypeGetRes> getTypeById(@PathVariable Integer id) {
        return companyService.getTypeById(id);
    }

    @PostMapping("types")
    public ResponseEntity<CompanyTypeGetRes> createType(@Valid @RequestBody CompanyTypeCreateReq request) {
        return companyService.saveType(request);
    }

    @PutMapping("types")
    public ResponseEntity<CompanyTypeDTO> updateType(@Valid @RequestBody CompanyTypeDTO request) {
        return companyService.updateType(request);
    }

    @DeleteMapping("types/{id}")
    public Integer deleteType(@Positive @PathVariable Integer id) {
        if (Objects.isNull(id)) {
            throw new IllegalArgumentException("Company type id cannot be null");
        }
        return companyService.deleteType(id);
    }

    @PostMapping("/{companyId}/{userId}")
    public ResponseEntity<UserDTO> addUserToCompany(@Valid @RequestBody AddUserToCompanyReq request) {
        return companyEmployeeService.addUserToCompany(request);
    }

    @GetMapping("/{companyId}/employees")
    public ResponseEntity<List<UserDTO>> getEmployeesByCompany(@PathVariable Integer companyId) {
        return companyEmployeeService.getEmployeesByCompany(companyId);
    }
}
