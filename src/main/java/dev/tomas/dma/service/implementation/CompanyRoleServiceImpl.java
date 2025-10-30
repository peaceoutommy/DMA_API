package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.CompanyRoleDTO;
import dev.tomas.dma.dto.common.CompanyRolePermissionDTO;
import dev.tomas.dma.dto.request.CompanyPermissionCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.response.*;
import dev.tomas.dma.entity.CompanyRole;
import dev.tomas.dma.entity.CompanyRolePermission;
import dev.tomas.dma.enums.PermissionType;
import dev.tomas.dma.repository.CompanyRolePermissionRepo;
import dev.tomas.dma.repository.CompanyRoleRepo;
import dev.tomas.dma.service.CompanyRoleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class CompanyRoleServiceImpl implements CompanyRoleService {
    private final CompanyRoleRepo companyRoleRepo;
    private final CompanyRolePermissionRepo companyRolePermissionRepo;

    public Optional<CompanyRoleGetAllRes> getAllByCompanyId(Integer companyId) {
        CompanyRoleGetAllRes response = new CompanyRoleGetAllRes();

        for (CompanyRole entity : companyRoleRepo.findAll()) {
            CompanyRoleDTO dto = new CompanyRoleDTO(entity.getId(), entity.getName(), entity.getCompany().getId());
            response.getRoles().add(dto);
        }
        return Optional.of(response);
    }

    public Optional<CompanyRoleCreateRes> create(CompanyRoleCreateReq request) {
        if (companyRoleRepo.findByCompanyIdAndName(request.getCompanyId(), request.getName()).isPresent()) {
            throw new DuplicateKeyException("Role with name '" + request.getName() + "' already exists for this company");
        }

        CompanyRole toSave = new CompanyRole();
        toSave.setName(request.getName());
        CompanyRole saved = companyRoleRepo.save(toSave);

        CompanyRoleCreateRes response = new CompanyRoleCreateRes();
        response.setId(saved.getId());
        response.setName(saved.getName());
        return Optional.of(response);
    }

    public Integer delete(Integer id) {
        companyRoleRepo.deleteById(id);
        return id;
    }

    public Optional<CompanyRolePermissionGetAllRes> getAllPermissions() {
        CompanyRolePermissionGetAllRes response = new CompanyRolePermissionGetAllRes();

        for (CompanyRolePermission entity : companyRolePermissionRepo.findAll()) {
            CompanyRolePermissionDTO dto = new CompanyRolePermissionDTO(entity.getId(), entity.getName(), entity.getType().toString(), entity.getDescription());
            response.getPermissions().add(dto);
        }

        return Optional.of(response);
    }

    public Optional<CompanyPermissionCreateRes> createPermission(@Valid CompanyPermissionCreateReq request) {
        CompanyRolePermission toSave = new CompanyRolePermission();
        toSave.setName(request.getName());
        toSave.setType(PermissionType.valueOf(request.getType()));
        toSave.setDescription(request.getDescription());
        companyRolePermissionRepo.save(toSave);

        CompanyPermissionCreateRes response = new CompanyPermissionCreateRes();
        response.setId(toSave.getId());
        response.setName(toSave.getName());
        response.setDescription(toSave.getDescription());
        return Optional.of(response);
    }

    public Optional<PermissionTypeGetAllRes> getAllPermissionTypes() {
        PermissionTypeGetAllRes response = new PermissionTypeGetAllRes();
        response.setTypes(Arrays.stream(PermissionType.values()).map(PermissionType::name).toList());
        return Optional.of(response);
    }
}
