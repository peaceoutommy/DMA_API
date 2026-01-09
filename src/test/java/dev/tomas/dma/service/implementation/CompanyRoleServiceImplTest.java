package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.CompanyPermissionDTO;
import dev.tomas.dma.dto.common.CompanyRoleDTO;
import dev.tomas.dma.dto.request.CompanyPermissionCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleUpdateReq;
import dev.tomas.dma.dto.response.CompanyRoleGetAllRes;
import dev.tomas.dma.dto.response.CompanyRolePermissionGetAllRes;
import dev.tomas.dma.dto.response.PermissionTypeGetAllRes;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.entity.CompanyPermission;
import dev.tomas.dma.entity.CompanyRole;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.enums.PermissionType;
import dev.tomas.dma.mapper.CompanyPermissionMapper;
import dev.tomas.dma.mapper.CompanyRoleMapper;
import dev.tomas.dma.repository.CompanyPermissionRepo;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.repository.CompanyRoleRepo;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyRoleServiceImplTest {

    @Mock
    private CompanyRoleRepo companyRoleRepo;

    @Mock
    private CompanyPermissionRepo companyPermissionRepo;

    @Mock
    private CompanyRepo companyRepo;

    @Mock
    private CompanyPermissionMapper permissionMapper;

    @Mock
    private CompanyRoleMapper roleMapper;

    @InjectMocks
    private CompanyRoleServiceImpl companyRoleService;

    private Company testCompany;
    private CompanyRole testRole;
    private CompanyPermission testPermission;
    private CompanyRoleDTO testRoleDTO;
    private CompanyRoleCreateReq createRequest;
    private CompanyRoleUpdateReq updateRequest;

    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setId(1);
        testCompany.setName("Test Company");

        testPermission = new CompanyPermission();
        testPermission.setId(1);
        testPermission.setName("Create Campaign");
        testPermission.setType(PermissionType.DONATION_CAMPAIGN_MANAGEMENT);
        testPermission.setDescription("Permission to create campaigns");

        testRole = new CompanyRole();
        testRole.setId(1);
        testRole.setName("Manager");
        testRole.setCompany(testCompany);
        testRole.setPermissions(Arrays.asList(testPermission));
        testRole.setUsers(new ArrayList<>());

        testRoleDTO = new CompanyRoleDTO();
        testRoleDTO.setId(1);
        testRoleDTO.setName("Manager");
        testRoleDTO.setCompanyId(1);

        createRequest = new CompanyRoleCreateReq();
        createRequest.setCompanyId(1);
        createRequest.setName("New Role");
        createRequest.setPermissionIds(Arrays.asList(1));

        updateRequest = new CompanyRoleUpdateReq();
        updateRequest.setId(1);
        updateRequest.setName("Updated Role");
        updateRequest.setPermissionIds(Arrays.asList(1));
    }

    @Nested
    @DisplayName("GetAllByCompanyId Tests")
    class GetAllByCompanyIdTests {

        @Test
        @DisplayName("Should return all roles for company")
        void getAllByCompanyId_Success() {
            when(companyRoleRepo.findAllByCompanyId(1)).thenReturn(Arrays.asList(testRole));
            when(permissionMapper.toDtos(any())).thenReturn(new ArrayList<>());

            CompanyRoleGetAllRes result = companyRoleService.getAllByCompanyId(1);

            assertThat(result).isNotNull();
            assertThat(result.getRoles()).hasSize(1);
            assertThat(result.getRoles().get(0).getName()).isEqualTo("Manager");
        }

        @Test
        @DisplayName("Should return empty list when no roles exist")
        void getAllByCompanyId_EmptyList() {
            when(companyRoleRepo.findAllByCompanyId(1)).thenReturn(new ArrayList<>());

            CompanyRoleGetAllRes result = companyRoleService.getAllByCompanyId(1);

            assertThat(result.getRoles()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Create Role Tests")
    class CreateRoleTests {

        @Test
        @DisplayName("Should create role successfully")
        void create_Success() {
            when(companyRoleRepo.findByCompanyIdAndName(1, "New Role")).thenReturn(null);
            when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
            when(companyPermissionRepo.findAllById(anyList())).thenReturn(Arrays.asList(testPermission));
            when(companyRoleRepo.save(any(CompanyRole.class))).thenAnswer(invocation -> {
                CompanyRole role = invocation.getArgument(0);
                role.setId(1);
                return role;
            });
            when(roleMapper.toDTO(any(CompanyRole.class))).thenReturn(testRoleDTO);

            CompanyRoleDTO result = companyRoleService.create(createRequest);

            assertThat(result).isNotNull();
            verify(companyRoleRepo).save(any(CompanyRole.class));
        }

        @Test
        @DisplayName("Should create role without permissions")
        void create_Success_WithoutPermissions() {
            createRequest.setPermissionIds(null);

            when(companyRoleRepo.findByCompanyIdAndName(1, "New Role")).thenReturn(null);
            when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
            when(companyRoleRepo.save(any(CompanyRole.class))).thenAnswer(invocation -> {
                CompanyRole role = invocation.getArgument(0);
                role.setId(1);
                return role;
            });
            when(roleMapper.toDTO(any(CompanyRole.class))).thenReturn(testRoleDTO);

            CompanyRoleDTO result = companyRoleService.create(createRequest);

            assertThat(result).isNotNull();
            verify(companyPermissionRepo, never()).findAllById(anyList());
        }

        @Test
        @DisplayName("Should throw DuplicateKeyException when role name exists")
        void create_ThrowsException_WhenRoleNameExists() {
            when(companyRoleRepo.findByCompanyIdAndName(1, "New Role")).thenReturn(testRole);

            assertThatThrownBy(() -> companyRoleService.create(createRequest))
                    .isInstanceOf(DuplicateKeyException.class)
                    .hasMessageContaining("Role with name 'New Role' already exists");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when company id is null")
        void create_ThrowsException_WhenCompanyIdNull() {
            createRequest.setCompanyId(null);

            assertThatThrownBy(() -> companyRoleService.create(createRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Company id cannot be null");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when company not found")
        void create_ThrowsException_WhenCompanyNotFound() {
            when(companyRoleRepo.findByCompanyIdAndName(1, "New Role")).thenReturn(null);
            when(companyRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyRoleService.create(createRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Company not found with id: 1");
        }
    }

    @Nested
    @DisplayName("Update Role Tests")
    class UpdateRoleTests {

        @Test
        @DisplayName("Should update role successfully")
        void update_Success() {
            when(companyRoleRepo.findById(1)).thenReturn(Optional.of(testRole));
            when(companyPermissionRepo.findAllById(anyList())).thenReturn(Arrays.asList(testPermission));
            when(companyRoleRepo.save(any(CompanyRole.class))).thenReturn(testRole);
            when(roleMapper.toDTO(any(CompanyRole.class))).thenReturn(testRoleDTO);

            CompanyRoleDTO result = companyRoleService.update(updateRequest);

            assertThat(result).isNotNull();
            verify(companyRoleRepo).save(argThat(role -> role.getName().equals("Updated Role")));
        }

        @Test
        @DisplayName("Should update role without changing permissions")
        void update_Success_WithoutPermissions() {
            updateRequest.setPermissionIds(null);

            when(companyRoleRepo.findById(1)).thenReturn(Optional.of(testRole));
            when(companyRoleRepo.save(any(CompanyRole.class))).thenReturn(testRole);
            when(roleMapper.toDTO(any(CompanyRole.class))).thenReturn(testRoleDTO);

            CompanyRoleDTO result = companyRoleService.update(updateRequest);

            assertThat(result).isNotNull();
            verify(companyPermissionRepo, never()).findAllById(anyList());
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when role not found")
        void update_ThrowsException_WhenRoleNotFound() {
            when(companyRoleRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyRoleService.update(updateRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Company role not found with id: 1");
        }
    }

    @Nested
    @DisplayName("Delete Role Tests")
    class DeleteRoleTests {

        @Test
        @DisplayName("Should delete role successfully")
        void delete_Success() {
            testRole.setUsers(new ArrayList<>());
            when(companyRoleRepo.findById(1)).thenReturn(Optional.of(testRole));
            when(companyRoleRepo.save(any(CompanyRole.class))).thenReturn(testRole);
            doNothing().when(companyRoleRepo).delete(any(CompanyRole.class));

            Integer result = companyRoleService.delete(1);

            assertThat(result).isEqualTo(1);
            verify(companyRoleRepo).save(argThat(role -> role.getPermissions() == null));
            verify(companyRoleRepo).delete(testRole);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when role not found")
        void delete_ThrowsException_WhenRoleNotFound() {
            when(companyRoleRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyRoleService.delete(999))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Role not found with id: 999");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when role has users assigned")
        void delete_ThrowsException_WhenRoleHasUsers() {
            User user = new User();
            user.setId(1);
            testRole.setUsers(Arrays.asList(user));

            when(companyRoleRepo.findById(1)).thenReturn(Optional.of(testRole));

            assertThatThrownBy(() -> companyRoleService.delete(1))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot delete role. 1 user(s) are assigned to this role");
        }
    }

    @Nested
    @DisplayName("Permission Tests")
    class PermissionTests {

        @Test
        @DisplayName("Should return all permissions")
        void getAllPermissions_Success() {
            when(companyPermissionRepo.findAll()).thenReturn(Arrays.asList(testPermission));

            CompanyRolePermissionGetAllRes result = companyRoleService.getAllPermissions();

            assertThat(result).isNotNull();
            assertThat(result.getPermissions()).hasSize(1);
            assertThat(result.getPermissions().get(0).getName()).isEqualTo("Create Campaign");
        }

        @Test
        @DisplayName("Should return all permissions as entities")
        void getAllPermissionsEntity_Success() {
            when(companyPermissionRepo.findAll()).thenReturn(Arrays.asList(testPermission));

            List<CompanyPermission> result = companyRoleService.getAllPermissionsEntity();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Create Campaign");
        }

        @Test
        @DisplayName("Should create permission successfully")
        void createPermission_Success() {
            CompanyPermissionCreateReq request = new CompanyPermissionCreateReq();
            request.setName("New Permission");
            request.setType("DONATION_REQUEST");
            request.setDescription("New permission description");

            when(companyPermissionRepo.save(any(CompanyPermission.class))).thenAnswer(invocation -> {
                CompanyPermission perm = invocation.getArgument(0);
                perm.setId(2);
                return perm;
            });

            var result = companyRoleService.createPermission(request);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("New Permission");
        }

        @Test
        @DisplayName("Should update permission successfully")
        void updatePermission_Success() {
            CompanyPermissionDTO request = new CompanyPermissionDTO(1, "Updated Permission", "DONATION_APPROVE", "Updated desc");

            when(companyPermissionRepo.findById(1)).thenReturn(Optional.of(testPermission));
            when(companyPermissionRepo.save(any(CompanyPermission.class))).thenReturn(testPermission);

            CompanyPermissionDTO result = companyRoleService.updatePermission(request);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when updating non-existent permission")
        void updatePermission_ThrowsException_WhenNotFound() {
            CompanyPermissionDTO request = new CompanyPermissionDTO(999, "Test", "DONATION_REQUEST", "Desc");

            when(companyPermissionRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyRoleService.updatePermission(request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Permission not found with id: 999");
        }

        @Test
        @DisplayName("Should delete permission successfully")
        void deletePermission_Success() {
            doNothing().when(companyPermissionRepo).deleteById(1);

            Integer result = companyRoleService.deletePermission(1);

            assertThat(result).isEqualTo(1);
            verify(companyPermissionRepo).deleteById(1);
        }

        @Test
        @DisplayName("Should return all permission types")
        void getAllPermissionTypes_Success() {
            PermissionTypeGetAllRes result = companyRoleService.getAllPermissionTypes();

            assertThat(result).isNotNull();
            assertThat(result.getTypes()).contains(
                    "COMPANY_ROLE_MANAGEMENT",
                    "EMPLOYEE_MANAGEMENT",
                    "DONATION_REQUEST",
                    "DONATION_APPROVE"
            );
        }
    }
}
