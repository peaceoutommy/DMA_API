package dev.tomas.dma.service;

import dev.tomas.dma.dto.common.CompanyPermissionDTO;
import dev.tomas.dma.dto.common.CompanyRoleDTO;
import dev.tomas.dma.dto.request.CompanyPermissionCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.response.*;
import dev.tomas.dma.entity.CompanyPermission;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CompanyRoleService {
    ResponseEntity<CompanyRoleGetAllRes> getAllByCompanyId(Integer companyId);
    ResponseEntity<CompanyRoleDTO> create(@Valid CompanyRoleCreateReq request);
    Integer delete(Integer id);

    ResponseEntity<CompanyRolePermissionGetAllRes> getAllPermissions();
    ResponseEntity<CompanyPermissionCreateRes> createPermission(@Valid CompanyPermissionCreateReq request);
    ResponseEntity<CompanyPermissionDTO> updatePermission(@Valid CompanyPermissionDTO request);
    Integer deletePermission(Integer id);
    ResponseEntity<PermissionTypeGetAllRes> getAllPermissionTypes();

    // Internal methods
    List<CompanyPermission> getAllPermissionsEntity();
}
