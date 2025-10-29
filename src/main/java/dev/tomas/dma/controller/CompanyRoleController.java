package dev.tomas.dma.controller;

import dev.tomas.dma.dto.request.CompanyPermissionCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.response.CompanyPermissionCreateRes;
import dev.tomas.dma.dto.response.CompanyRoleCreateRes;
import dev.tomas.dma.dto.response.CompanyRoleGetAllRes;
import dev.tomas.dma.dto.response.CompanyRolePermissionGetAllRes;
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
    public Optional<CompanyRoleGetAllRes> getAllByCompanyId(@PathVariable Integer companyId) {
        return companyRoleService.getAllByCompanyId(companyId);
    }

    @PostMapping
    public Optional<CompanyRoleCreateRes> create(@RequestBody @Valid CompanyRoleCreateReq request) {
        return companyRoleService.create(request);
    }

    @DeleteMapping("/{id}")
    public Integer delete(@PathVariable Integer id) {
        return companyRoleService.delete(id);
    }

    @GetMapping("/permissions")
    public Optional<CompanyRolePermissionGetAllRes> getPermissions() {
        return companyRoleService.getAllPermissions();
    }

    @PostMapping("/permission")
    public Optional<CompanyPermissionCreateRes> createPermission(@RequestBody @Valid CompanyPermissionCreateReq request) {
        return companyRoleService.createPermission(request);
    }
}
