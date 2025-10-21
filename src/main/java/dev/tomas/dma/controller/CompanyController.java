package dev.tomas.dma.controller;

import dev.tomas.dma.dto.common.CompanyTypeDTO;
import dev.tomas.dma.dto.request.AddUserToCompanyReq;
import dev.tomas.dma.dto.request.CompanyCreateReq;
import dev.tomas.dma.dto.request.CompanyTypeCreateReq;
import dev.tomas.dma.dto.response.*;
import dev.tomas.dma.service.CompanyService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyService companyService;

    @GetMapping
    public CompanyGetAllRes getAll(){
        return companyService.getAll();
    }

    @PostMapping
    public CompanyCreateRes create(@Valid @RequestBody CompanyCreateReq request) {
        return companyService.save(request);
    }

    @GetMapping("types")
    public Optional<CompanyTypeGetAllRes> getAllTypes(){return companyService.getAllTypes();}

    @GetMapping("types/{id}")
    public Optional<CompanyTypeGetRes> getTypeById(@PathVariable Integer id){
        return companyService.getTypeById(id);
    }

    @PostMapping("types")
    public CompanyTypeGetRes createType(@Valid @RequestBody CompanyTypeCreateReq request) {
        return companyService.saveType(request);
    }

    @PutMapping("types")
    public Optional<CompanyTypeDTO> updateType(@Valid @RequestBody CompanyTypeDTO request) {
        return companyService.updateType(request);
    }

    @DeleteMapping("types/{id}")
    public Integer deleteType(@Positive @PathVariable Integer id){
        if (Objects.isNull(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company type id cannot be null");
        }
        return companyService.deleteType(id);
    }

    @PostMapping("/users")
    public Optional<AddUserToCompanyRes> addUserToCompany(@Valid @RequestBody AddUserToCompanyReq request) {
        return companyService.addUserToCompany(request);
    }
}
