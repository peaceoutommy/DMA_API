package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.CompanyPermissionDTO;
import dev.tomas.dma.dto.common.CompanyRoleDTO;
import dev.tomas.dma.dto.request.CompanyPermissionCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleUpdateReq;
import dev.tomas.dma.dto.response.*;
import dev.tomas.dma.entity.*;
import dev.tomas.dma.enums.PermissionType;
import dev.tomas.dma.mapper.CompanyPermissionMapper;
import dev.tomas.dma.mapper.CompanyRoleMapper;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.repository.CompanyPermissionRepo;
import dev.tomas.dma.repository.CompanyRoleRepo;
import dev.tomas.dma.service.CompanyRoleService;
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
    private final CompanyPermissionRepo companyPermissionRepo;
    private final CompanyRepo companyRepo;
    private final CompanyPermissionMapper permissionMapper;
    private final CompanyRoleMapper roleMapper;

    public ResponseEntity<CompanyRoleGetAllRes> getAllByCompanyId(Integer companyId) {
        CompanyRoleGetAllRes response = new CompanyRoleGetAllRes();

        for (CompanyRole entity : companyRoleRepo.findAllByCompanyId(companyId)) {
            CompanyRoleDTO roleDto = new CompanyRoleDTO();
            roleDto.setId(entity.getId());
            roleDto.setName(entity.getName());
            roleDto.setCompanyId(entity.getCompany().getId());
            roleDto.setPermissions(permissionMapper.toDtos(entity.getPermissions()));
            response.getRoles().add(roleDto);
        }
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<CompanyRoleDTO> create(CompanyRoleCreateReq request) {
        if (companyRoleRepo.findByCompanyIdAndName(request.getCompanyId(), request.getName()) != null) {
            throw new DuplicateKeyException("Role with name '" + request.getName() + "' already exists for this company");
        }
        if (request.getCompanyId() == null) {
            throw new IllegalArgumentException("Company id cannot be null");
        }
        Company company = companyRepo.findById(request.getCompanyId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + request.getCompanyId()));

        CompanyRole toSave = new CompanyRole();
        toSave.setName(request.getName());
        toSave.setCompany(company);

        if (request.getPermissionIds() != null) {
            List<CompanyPermission> permissions = companyPermissionRepo.findAllById(request.getPermissionIds());
            toSave.setPermissions(permissions);
        }

        CompanyRole saved = companyRoleRepo.save(toSave);
        CompanyRoleDTO dto = roleMapper.toDTO(saved);
        return ResponseEntity.ok(dto);
    }

    public CompanyRoleDTO update(CompanyRoleUpdateReq request) {
        CompanyRole toSave = companyRoleRepo.findById(request.getId()).orElseThrow(() -> new EntityNotFoundException("Company role not found with id: " + request.getId()));

        toSave.setName(request.getName());
        if (request.getPermissionIds() != null) {
            toSave.setPermissions(companyPermissionRepo.findAllById(request.getPermissionIds()));
        }

        CompanyRole saved = companyRoleRepo.save(toSave);
        return roleMapper.toDTO(saved);
    }

    public List<CompanyPermission> getAllPermissionsEntity() {
        return new ArrayList<>(companyPermissionRepo.findAll());
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

        for (CompanyPermission entity : companyPermissionRepo.findAll()) {
            CompanyPermissionDTO dto = new CompanyPermissionDTO(entity.getId(), entity.getName(), entity.getType().toString(), entity.getDescription());
            response.getPermissions().add(dto);
        }

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<CompanyPermissionCreateRes> createPermission(@Valid CompanyPermissionCreateReq request) {
        CompanyPermission toSave = new CompanyPermission();
        toSave.setName(request.getName());
        toSave.setType(PermissionType.valueOf(request.getType()));
        toSave.setDescription(request.getDescription());

        CompanyPermission entity = companyPermissionRepo.save(toSave);

        CompanyPermissionCreateRes response = new CompanyPermissionCreateRes();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<CompanyPermissionDTO> updatePermission(@Valid CompanyPermissionDTO request) {
        CompanyPermission toUpdate = companyPermissionRepo.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Permission not found with id: " + request.getId()));

        toUpdate.setName(request.getName());
        toUpdate.setType(PermissionType.valueOf(request.getType()));
        toUpdate.setDescription(request.getDescription());

        CompanyPermission entity = companyPermissionRepo.save(toUpdate);

        CompanyPermissionDTO response = new CompanyPermissionDTO();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        return ResponseEntity.ok(response);
    }

    public Integer deletePermission(Integer id) {
        companyPermissionRepo.deleteById(id);
        return id;
    }

    public ResponseEntity<PermissionTypeGetAllRes> getAllPermissionTypes() {
        PermissionTypeGetAllRes response = new PermissionTypeGetAllRes();
        response.setTypes(Arrays.stream(PermissionType.values()).map(PermissionType::name).toList());
        return ResponseEntity.ok(response);
    }
}
