package dev.tomas.dma.service;

import dev.tomas.dma.dto.common.CompanyPermissionDTO;
import dev.tomas.dma.dto.common.CompanyRoleDTO;
import dev.tomas.dma.dto.request.CompanyPermissionCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleUpdateReq;
import dev.tomas.dma.dto.response.*;
import dev.tomas.dma.entity.CompanyPermission;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CompanyRoleService {
    CompanyRoleGetAllRes getAllByCompanyId(Integer companyId);
    CompanyRoleDTO create(CompanyRoleCreateReq request);
    CompanyRoleDTO update(CompanyRoleUpdateReq request);
    Integer delete(Integer id);

    CompanyRolePermissionGetAllRes getAllPermissions();
    CompanyPermissionCreateRes createPermission(CompanyPermissionCreateReq request);
    CompanyPermissionDTO updatePermission(CompanyPermissionDTO request);
    Integer deletePermission(Integer id);
    PermissionTypeGetAllRes getAllPermissionTypes();

    // Internal methods
    List<CompanyPermission> getAllPermissionsEntity();
}
