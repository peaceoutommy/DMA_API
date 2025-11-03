package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.CompanyRoleDTO;
import dev.tomas.dma.dto.common.CompanyRolePermissionDTO;
import dev.tomas.dma.dto.request.CompanyPermissionCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.response.*;
import dev.tomas.dma.entity.*;
import dev.tomas.dma.enums.PermissionType;
import dev.tomas.dma.mapper.CompanyRolePermissionMapper;
import dev.tomas.dma.repository.CompanyRolePermissionRepo;
import dev.tomas.dma.repository.CompanyRoleRepo;
import dev.tomas.dma.repository.UserCompanyMembershipRepo;
import dev.tomas.dma.service.CompanyRoleService;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.apache.catalina.mapper.Mapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class CompanyRoleServiceImpl implements CompanyRoleService {
    private final CompanyRoleRepo companyRoleRepo;
    private final CompanyRolePermissionRepo companyRolePermissionRepo;
    private final UserCompanyMembershipRepo membershipRepo;
    private final CompanyRolePermissionMapper permissionMapper;
    private final EntityManager entityManager;

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

        CompanyRole role = saveRoleEntity(request);

        CompanyRoleCreateRes response = new CompanyRoleCreateRes();
        response.setId(role.getId());
        response.setName(role.getName());
        return Optional.of(response);
    }

    protected CompanyRole saveRoleEntity(CompanyRoleCreateReq request) {
        if (request.getCompanyId() == null) {
            throw new IllegalArgumentException("Company ID cannot be null");
        }

        CompanyRole toSave = new CompanyRole();
        toSave.setName(request.getName());
        toSave.setCompany(entityManager.getReference(Company.class, request.getCompanyId()));

        if (request.getPermissions() != null) {
            for (CompanyRolePermissionDTO dto : request.getPermissions()) {
               CompanyRolePermission entity = permissionMapper.toEntity(dto);
               toSave.getPermissions().add(entity);
            }
        }
        return companyRoleRepo.save(toSave);
    }

    @Transactional
    protected CompanyRole saveRoleEntity(CompanyRole role, Integer userId){
        CompanyRole savedRole = companyRoleRepo.save(role);

        UserCompanyMembership membership = new UserCompanyMembership();
        membership.setUser(entityManager.getReference(User.class, userId));
        membership.setCompany(savedRole.getCompany());
        membership.setCompanyRole(savedRole);
        membershipRepo.save(membership);

        return savedRole;
    }

    protected List<CompanyRolePermission> getAllPermissionsEntity(){
        List<CompanyRolePermission> permissions = new ArrayList<>();
        for (CompanyRolePermission entity : companyRolePermissionRepo.findAll()) {
            permissions.add(entity);
        }
        return permissions;
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

        CompanyRolePermission entity =  companyRolePermissionRepo.save(toSave);

        CompanyPermissionCreateRes response = new CompanyPermissionCreateRes();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        return Optional.of(response);
    }

    public Optional<CompanyRolePermissionDTO> updatePermission(@Valid CompanyRolePermissionDTO request) {
        CompanyRolePermission toSave = new CompanyRolePermission();
        toSave.setId(request.getId());
        toSave.setName(request.getName());
        toSave.setType(PermissionType.valueOf(request.getType()));
        toSave.setDescription(request.getDescription());

        CompanyRolePermission entity = companyRolePermissionRepo.save(toSave);

        CompanyRolePermissionDTO response = new CompanyRolePermissionDTO();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        return Optional.of(response);
    }

    public Integer deletePermission(Integer id) {
        companyRolePermissionRepo.deleteById(id);
        return id;
    }

    public Optional<PermissionTypeGetAllRes> getAllPermissionTypes() {
        PermissionTypeGetAllRes response = new PermissionTypeGetAllRes();
        response.setTypes(Arrays.stream(PermissionType.values()).map(PermissionType::name).toList());
        return Optional.of(response);
    }
}
