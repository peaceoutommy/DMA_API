package dev.tomas.dma.controller;

import dev.tomas.dma.dto.common.CompanyPermissionDTO;
import dev.tomas.dma.dto.common.CompanyRoleDTO;
import dev.tomas.dma.dto.request.CompanyPermissionCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleUpdateReq;
import dev.tomas.dma.dto.response.*;
import dev.tomas.dma.service.CompanyRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies/roles")
public class CompanyRoleController {
    private final CompanyRoleService companyRoleService;

    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyRoleGetAllRes> getAllByCompanyId(@PathVariable Integer companyId) {
        return companyRoleService.getAllByCompanyId(companyId);
    }

    @PostMapping
    public ResponseEntity<CompanyRoleDTO> create(@RequestBody @Valid CompanyRoleCreateReq request) {
        return companyRoleService.create(request);
    }

    @PutMapping
    public ResponseEntity<CompanyRoleDTO> update(@RequestBody @Valid CompanyRoleUpdateReq request) {
        return ResponseEntity.ok(companyRoleService.update(request));
    }

    @DeleteMapping("/{id}")
    public Integer delete(@PathVariable Integer id) {
        return companyRoleService.delete(id);
    }

    @GetMapping("/permissions")
    public ResponseEntity<CompanyRolePermissionGetAllRes> getPermissions() {
        return companyRoleService.getAllPermissions();
    }

    @PostMapping("/permission")
    public ResponseEntity<CompanyPermissionCreateRes> createPermission(@RequestBody @Valid CompanyPermissionCreateReq request) {
        return companyRoleService.createPermission(request);
    }

    @PutMapping("/permission")
    public ResponseEntity<CompanyPermissionDTO> updatePermission(@RequestBody @Valid CompanyPermissionDTO request) {
        return companyRoleService.updatePermission(request);
    }

    @DeleteMapping("/permission/{id}")
    public Integer deletePermission(@PathVariable Integer id) {
        return companyRoleService.deletePermission(id);
    }

    @GetMapping("/permissions/types")
    public ResponseEntity<PermissionTypeGetAllRes> getPermissionTypes() {
        return companyRoleService.getAllPermissionTypes();
    }
}
