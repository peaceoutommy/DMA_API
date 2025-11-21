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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/companies/roles")
public class CompanyRoleController {
    private final CompanyRoleService companyRoleService;

    @PreAuthorize("hasAuthority('PERMISSION_List roles')")
    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyRoleGetAllRes> getAllByCompanyId(@PathVariable Integer companyId) {
        return ResponseEntity.ok(companyRoleService.getAllByCompanyId(companyId));
    }

    @PreAuthorize("hasAuthority('PERMISSION_Create role')")
    @PostMapping
    public ResponseEntity<CompanyRoleDTO> create(@RequestBody @Valid CompanyRoleCreateReq request) {
        return ResponseEntity.ok(companyRoleService.create(request));
    }

    @PreAuthorize("hasAuthority('PERMISSION_Modify role')")
    @PutMapping
    public ResponseEntity<CompanyRoleDTO> update(@RequestBody @Valid CompanyRoleUpdateReq request) {
        return ResponseEntity.ok(companyRoleService.update(request));
    }

    @PreAuthorize("hasAuthority('PERMISSION_Delete role')")
    @DeleteMapping("/{id}")
    public Integer delete(@PathVariable Integer id) {
        return companyRoleService.delete(id);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/permissions")
    public ResponseEntity<CompanyRolePermissionGetAllRes> getPermissions() {
        return ResponseEntity.ok(companyRoleService.getAllPermissions());
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/permission")
    public ResponseEntity<CompanyPermissionCreateRes> createPermission(@RequestBody @Valid CompanyPermissionCreateReq request) {
        return ResponseEntity.ok(companyRoleService.createPermission(request));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/permission")
    public ResponseEntity<CompanyPermissionDTO> updatePermission(@RequestBody @Valid CompanyPermissionDTO request) {
        return ResponseEntity.ok(companyRoleService.updatePermission(request));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/permission/{id}")
    public Integer deletePermission(@PathVariable Integer id) {
        return companyRoleService.deletePermission(id);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping("/permissions/types")
    public ResponseEntity<PermissionTypeGetAllRes> getPermissionTypes() {
        return ResponseEntity.ok(companyRoleService.getAllPermissionTypes());
    }
}
