package dev.tomas.dma.service;

import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.response.CompanyRoleCreateRes;
import dev.tomas.dma.dto.response.CompanyRoleGetAllRes;
import jakarta.validation.Valid;

import java.util.Optional;

public interface CompanyRoleService {
    Optional<CompanyRoleGetAllRes> getAll(Integer companyId);
    Optional<CompanyRoleCreateRes> save(@Valid CompanyRoleCreateReq request);
    Integer delete(Integer id);
}
