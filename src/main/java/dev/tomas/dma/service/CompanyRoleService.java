package dev.tomas.dma.service;

import dev.tomas.dma.dto.common.CompanyRolePermissionDTO;
import dev.tomas.dma.dto.request.CompanyPermissionCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.response.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface CompanyRoleService {
    ResponseEntity<CompanyRoleGetAllRes> getAllByCompanyId(Integer companyId);
    ResponseEntity<CompanyRoleCreateRes> create(@Valid CompanyRoleCreateReq request);
    Integer delete(Integer id);

    ResponseEntity<CompanyRolePermissionGetAllRes> getAllPermissions();
    ResponseEntity<CompanyPermissionCreateRes> createPermission(@Valid CompanyPermissionCreateReq request);
    ResponseEntity<CompanyRolePermissionDTO> updatePermission(@Valid CompanyRolePermissionDTO request);
    Integer deletePermission(Integer id);
    ResponseEntity<PermissionTypeGetAllRes> getAllPermissionTypes();
}
