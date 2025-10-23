package dev.tomas.dma.controller;

import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.response.CompanyRoleCreateRes;
import dev.tomas.dma.dto.response.CompanyRoleGetAllRes;
import dev.tomas.dma.service.CompanyRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies/roles")
public class CompanyRoleController {
    private final CompanyRoleService companyRoleService;

    @GetMapping("/{companyId}")
    public Optional<CompanyRoleGetAllRes> getAll(@PathVariable Integer companyId) {
        return companyRoleService.getAll(companyId);
    }

    @PostMapping
    public Optional<CompanyRoleCreateRes> create(@RequestBody @Valid CompanyRoleCreateReq request) {
        return companyRoleService.save(request);
    }

    @DeleteMapping("/{id}")
    public Integer delete(@PathVariable Integer id) {
        return companyRoleService.delete(id);
    }
}
