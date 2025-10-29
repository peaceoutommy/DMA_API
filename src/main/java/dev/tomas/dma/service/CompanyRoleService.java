package dev.tomas.dma.service;

import dev.tomas.dma.dto.request.CompanyPermissionCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.response.CompanyPermissionCreateRes;
import dev.tomas.dma.dto.response.CompanyRoleCreateRes;
import dev.tomas.dma.dto.response.CompanyRoleGetAllRes;
import dev.tomas.dma.dto.response.CompanyRolePermissionGetAllRes;
import jakarta.validation.Valid;

import java.util.Optional;

public interface CompanyRoleService {
    Optional<CompanyRoleGetAllRes> getAllByCompanyId(Integer companyId);
    Optional<CompanyRoleCreateRes> create(@Valid CompanyRoleCreateReq request);
    Integer delete(Integer id);

    Optional<CompanyRolePermissionGetAllRes> getAllPermissions();
    Optional<CompanyPermissionCreateRes> createPermission(@Valid CompanyPermissionCreateReq request);
}
