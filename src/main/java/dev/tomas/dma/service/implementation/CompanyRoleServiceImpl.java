package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.CompanyRoleDTO;
import dev.tomas.dma.dto.common.CompanyRolePermissionDTO;
import dev.tomas.dma.dto.request.CompanyPermissionCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.response.*;
import dev.tomas.dma.entity.*;
import dev.tomas.dma.enums.PermissionType;
import dev.tomas.dma.mapper.CompanyRolePermissionMapper;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.repository.CompanyRolePermissionRepo;
import dev.tomas.dma.repository.CompanyRoleRepo;
import dev.tomas.dma.repository.UserRepo;
import dev.tomas.dma.service.CompanyRoleService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Service
public class CompanyRoleServiceImpl implements CompanyRoleService {
    private final CompanyRoleRepo companyRoleRepo;
    private final CompanyRolePermissionRepo companyRolePermissionRepo;
    private final CompanyRepo  companyRepo;
    private final CompanyRolePermissionMapper permissionMapper;

    public ResponseEntity<CompanyRoleGetAllRes> getAllByCompanyId(Integer companyId) {
        CompanyRoleGetAllRes response = new CompanyRoleGetAllRes();

        for (CompanyRole entity : companyRoleRepo.findAll()) {
            CompanyRoleDTO dto = new CompanyRoleDTO(entity.getId(), entity.getName(), entity.getCompany().getId());
            response.getRoles().add(dto);
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<CompanyRoleCreateRes> create(CompanyRoleCreateReq request) {
        if (companyRoleRepo.findByCompanyIdAndName(request.getCompanyId(), request.getName()).isPresent()) {
            throw new DuplicateKeyException("Role with name '" + request.getName() + "' already exists for this company");
        }

        CompanyRole role = saveRoleEntity(request);

        CompanyRoleCreateRes response = new CompanyRoleCreateRes();
        response.setId(role.getId());
        response.setName(role.getName());
        return ResponseEntity.ok(response);
    }

    public CompanyRole saveRoleEntity(CompanyRoleCreateReq request) {
        if (request.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }

        Company company = companyRepo.findById(request.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + request.getCompanyId()));

        CompanyRole toSave = new CompanyRole();
        toSave.setName(request.getName());
        toSave.setCompany(company);

        if (request.getPermissions() != null) {
            for (CompanyRolePermissionDTO dto : request.getPermissions()) {
                CompanyPermission entity = permissionMapper.toEntity(dto);
                toSave.getPermissions().add(entity);
            }
        }
        return companyRoleRepo.save(toSave);
    }

    public List<CompanyPermission> getAllPermissionsEntity() {
        return new ArrayList<>(companyRolePermissionRepo.findAll());
    }

    @Transactional
    public Integer delete(Integer roleId) {
        CompanyRole role = companyRoleRepo.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found with id: " + roleId));

        if (!role.getUsers().isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete role. " + role.getUsers().size() + " user(s) are assigned to this role"
            );
        }

        role.setPermissions(null);
        companyRoleRepo.save(role);
        companyRoleRepo.delete(role);
        return roleId;
    }

    public ResponseEntity<CompanyRolePermissionGetAllRes> getAllPermissions() {
        CompanyRolePermissionGetAllRes response = new CompanyRolePermissionGetAllRes();

        for (CompanyPermission entity : companyRolePermissionRepo.findAll()) {
            CompanyRolePermissionDTO dto = new CompanyRolePermissionDTO(entity.getId(), entity.getName(), entity.getType().toString(), entity.getDescription());
            response.getPermissions().add(dto);
        }

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<CompanyPermissionCreateRes> createPermission(@Valid CompanyPermissionCreateReq request) {
        CompanyPermission toSave = new CompanyPermission();
        toSave.setName(request.getName());
        toSave.setType(PermissionType.valueOf(request.getType()));
        toSave.setDescription(request.getDescription());

        CompanyPermission entity = companyRolePermissionRepo.save(toSave);

        CompanyPermissionCreateRes response = new CompanyPermissionCreateRes();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<CompanyRolePermissionDTO> updatePermission(@Valid CompanyRolePermissionDTO request) {
        CompanyPermission toUpdate = companyRolePermissionRepo.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + request.getId()));

        toUpdate.setName(request.getName());
        toUpdate.setType(PermissionType.valueOf(request.getType()));
        toUpdate.setDescription(request.getDescription());

        CompanyPermission entity = companyRolePermissionRepo.save(toUpdate);

        CompanyRolePermissionDTO response = new CompanyRolePermissionDTO();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        return ResponseEntity.ok(response);
    }

    public Integer deletePermission(Integer id) {
        companyRolePermissionRepo.deleteById(id);
        return id;
    }

    public ResponseEntity<PermissionTypeGetAllRes> getAllPermissionTypes() {
        PermissionTypeGetAllRes response = new PermissionTypeGetAllRes();
        response.setTypes(Arrays.stream(PermissionType.values()).map(PermissionType::name).toList());
        return ResponseEntity.ok(response);
    }
}
