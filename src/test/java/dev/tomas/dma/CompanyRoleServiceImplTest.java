package dev.tomas.dma;

import dev.tomas.dma.dto.common.CompanyPermissionDTO;
import dev.tomas.dma.dto.common.CompanyRoleDTO;
import dev.tomas.dma.dto.request.CompanyPermissionCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleUpdateReq;
import dev.tomas.dma.dto.response.CompanyPermissionCreateRes;
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
import dev.tomas.dma.service.implementation.CompanyRoleServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
    private CompanyPermissionDTO testPermissionDTO;

    @BeforeEach
    void setUp() {
        // Setup test company
        testCompany = new Company();
        testCompany.setId(1);
        testCompany.setName("Test Company");

        // Setup test permission
        testPermission = new CompanyPermission();
        testPermission.setId(1);
        testPermission.setName("Manage Company Roles");
        testPermission.setType(PermissionType.COMPANY_ROLE_MANAGEMENT);
        testPermission.setDescription("Permission to manage company roles");

        // Setup test role
        testRole = new CompanyRole();
        testRole.setId(1);
        testRole.setName("Admin");
        testRole.setCompany(testCompany);
        testRole.setPermissions(new ArrayList<>(Arrays.asList(testPermission)));
        testRole.setUsers(new ArrayList<>());

        // Setup DTOs
        testPermissionDTO = new CompanyPermissionDTO(1, "Manage Company Roles", "COMPANY_ROLE_MANAGEMENT", "Permission to manage company roles");
        testRoleDTO = new CompanyRoleDTO();
        testRoleDTO.setId(1);
        testRoleDTO.setName("Admin");
        testRoleDTO.setCompanyId(1);
        testRoleDTO.setPermissions(Arrays.asList(testPermissionDTO));
    }

    @Test
    void getAllByCompanyId_ShouldReturnAllRoles() {
        // Arrange
        List<CompanyRole> roles = Arrays.asList(testRole);
        when(companyRoleRepo.findAllByCompanyId(1)).thenReturn(roles);
        when(permissionMapper.toDtos(anyList())).thenReturn(Arrays.asList(testPermissionDTO));

        // Act
        CompanyRoleGetAllRes result = companyRoleService.getAllByCompanyId(1);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getRoles());
        assertEquals(1, result.getRoles().size());
        assertEquals("Admin", result.getRoles().get(0).getName());
        assertEquals(1, result.getRoles().get(0).getCompanyId());
        verify(companyRoleRepo, times(1)).findAllByCompanyId(1);
        verify(permissionMapper, times(1)).toDtos(anyList());
    }

    @Test
    void getAllByCompanyId_ShouldReturnEmptyList_WhenNoRoles() {
        // Arrange
        when(companyRoleRepo.findAllByCompanyId(1)).thenReturn(new ArrayList<>());

        // Act
        CompanyRoleGetAllRes result = companyRoleService.getAllByCompanyId(1);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getRoles());
        assertTrue(result.getRoles().isEmpty());
        verify(companyRoleRepo, times(1)).findAllByCompanyId(1);
    }

    @Test
    void create_ShouldCreateRoleSuccessfully() {
        // Arrange
        CompanyRoleCreateReq request = new CompanyRoleCreateReq();
        request.setName("Manager");
        request.setCompanyId(1);
        request.setPermissionIds(Arrays.asList(1));

        when(companyRoleRepo.findByCompanyIdAndName(1, "Manager")).thenReturn(null);
        when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
        when(companyPermissionRepo.findAllById(anyList())).thenReturn(Arrays.asList(testPermission));
        when(companyRoleRepo.save(any(CompanyRole.class))).thenReturn(testRole);
        when(roleMapper.toDTO(any(CompanyRole.class))).thenReturn(testRoleDTO);

        // Act
        CompanyRoleDTO result = companyRoleService.create(request);

        // Assert
        assertNotNull(result);
        assertEquals("Admin", result.getName());
        verify(companyRoleRepo, times(1)).findByCompanyIdAndName(1, "Manager");
        verify(companyRepo, times(1)).findById(1);
        verify(companyPermissionRepo, times(1)).findAllById(anyList());
        verify(companyRoleRepo, times(1)).save(any(CompanyRole.class));
        verify(roleMapper, times(1)).toDTO(any(CompanyRole.class));
    }

    @Test
    void create_ShouldCreateRoleWithoutPermissions() {
        // Arrange
        CompanyRoleCreateReq request = new CompanyRoleCreateReq();
        request.setName("Employee");
        request.setCompanyId(1);
        request.setPermissionIds(null);

        when(companyRoleRepo.findByCompanyIdAndName(1, "Employee")).thenReturn(null);
        when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
        when(companyRoleRepo.save(any(CompanyRole.class))).thenReturn(testRole);
        when(roleMapper.toDTO(any(CompanyRole.class))).thenReturn(testRoleDTO);

        // Act
        CompanyRoleDTO result = companyRoleService.create(request);

        // Assert
        assertNotNull(result);
        verify(companyPermissionRepo, never()).findAllById(anyList());
        verify(companyRoleRepo, times(1)).save(any(CompanyRole.class));
    }

    @Test
    void create_ShouldThrowDuplicateKeyException_WhenRoleNameExists() {
        // Arrange
        CompanyRoleCreateReq request = new CompanyRoleCreateReq();
        request.setName("Admin");
        request.setCompanyId(1);

        when(companyRoleRepo.findByCompanyIdAndName(1, "Admin")).thenReturn(testRole);

        // Act & Assert
        assertThrows(DuplicateKeyException.class, () -> companyRoleService.create(request));
        verify(companyRoleRepo, times(1)).findByCompanyIdAndName(1, "Admin");
        verify(companyRoleRepo, never()).save(any(CompanyRole.class));
    }

    @Test
    void create_ShouldThrowIllegalArgumentException_WhenCompanyIdIsNull() {
        // Arrange
        CompanyRoleCreateReq request = new CompanyRoleCreateReq();
        request.setName("Admin");
        request.setCompanyId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> companyRoleService.create(request));
        verify(companyRoleRepo, never()).save(any(CompanyRole.class));
    }

    @Test
    void create_ShouldThrowEntityNotFoundException_WhenCompanyNotFound() {
        // Arrange
        CompanyRoleCreateReq request = new CompanyRoleCreateReq();
        request.setName("Manager");
        request.setCompanyId(999);

        when(companyRoleRepo.findByCompanyIdAndName(999, "Manager")).thenReturn(null);
        when(companyRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> companyRoleService.create(request));
        verify(companyRepo, times(1)).findById(999);
        verify(companyRoleRepo, never()).save(any(CompanyRole.class));
    }

    @Test
    void update_ShouldUpdateRoleSuccessfully() {
        // Arrange
        CompanyRoleUpdateReq request = new CompanyRoleUpdateReq();
        request.setId(1);
        request.setName("Updated Admin");
        request.setPermissionIds(Arrays.asList(1));

        when(companyRoleRepo.findById(1)).thenReturn(Optional.of(testRole));
        when(companyPermissionRepo.findAllById(anyList())).thenReturn(Arrays.asList(testPermission));
        when(companyRoleRepo.save(any(CompanyRole.class))).thenReturn(testRole);
        when(roleMapper.toDTO(any(CompanyRole.class))).thenReturn(testRoleDTO);

        // Act
        CompanyRoleDTO result = companyRoleService.update(request);

        // Assert
        assertNotNull(result);
        verify(companyRoleRepo, times(1)).findById(1);
        verify(companyPermissionRepo, times(1)).findAllById(anyList());
        verify(companyRoleRepo, times(1)).save(any(CompanyRole.class));
        verify(roleMapper, times(1)).toDTO(any(CompanyRole.class));
    }

    @Test
    void update_ShouldUpdateRoleWithoutChangingPermissions() {
        // Arrange
        CompanyRoleUpdateReq request = new CompanyRoleUpdateReq();
        request.setId(1);
        request.setName("Updated Admin");
        request.setPermissionIds(null);

        when(companyRoleRepo.findById(1)).thenReturn(Optional.of(testRole));
        when(companyRoleRepo.save(any(CompanyRole.class))).thenReturn(testRole);
        when(roleMapper.toDTO(any(CompanyRole.class))).thenReturn(testRoleDTO);

        // Act
        CompanyRoleDTO result = companyRoleService.update(request);

        // Assert
        assertNotNull(result);
        verify(companyPermissionRepo, never()).findAllById(anyList());
        verify(companyRoleRepo, times(1)).save(any(CompanyRole.class));
    }

    @Test
    void update_ShouldThrowEntityNotFoundException_WhenRoleNotFound() {
        // Arrange
        CompanyRoleUpdateReq request = new CompanyRoleUpdateReq();
        request.setId(999);
        request.setName("Updated Admin");

        when(companyRoleRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> companyRoleService.update(request));
        verify(companyRoleRepo, times(1)).findById(999);
        verify(companyRoleRepo, never()).save(any(CompanyRole.class));
    }

    @Test
    void getAllPermissionsEntity_ShouldReturnAllPermissions() {
        // Arrange
        List<CompanyPermission> permissions = Arrays.asList(testPermission);
        when(companyPermissionRepo.findAll()).thenReturn(permissions);

        // Act
        List<CompanyPermission> result = companyRoleService.getAllPermissionsEntity();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(companyPermissionRepo, times(1)).findAll();
    }

    @Test
    void delete_ShouldDeleteRoleSuccessfully() {
        // Arrange
        when(companyRoleRepo.findById(1)).thenReturn(Optional.of(testRole));
        when(companyRoleRepo.save(any(CompanyRole.class))).thenReturn(testRole);

        // Act
        Integer result = companyRoleService.delete(1);

        // Assert
        assertEquals(1, result);
        verify(companyRoleRepo, times(1)).findById(1);
        verify(companyRoleRepo, times(1)).save(any(CompanyRole.class));
        verify(companyRoleRepo, times(1)).delete(any(CompanyRole.class));
    }

    @Test
    void delete_ShouldThrowEntityNotFoundException_WhenRoleNotFound() {
        // Arrange
        when(companyRoleRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> companyRoleService.delete(999));
        verify(companyRoleRepo, times(1)).findById(999);
        verify(companyRoleRepo, never()).delete(any(CompanyRole.class));
    }

    @Test
    void delete_ShouldThrowIllegalStateException_WhenRoleHasUsers() {
        // Arrange
        User user = new User();
        user.setId(1);
        testRole.setUsers(Arrays.asList(user));

        when(companyRoleRepo.findById(1)).thenReturn(Optional.of(testRole));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> companyRoleService.delete(1)
        );
        assertTrue(exception.getMessage().contains("Cannot delete role"));
        verify(companyRoleRepo, times(1)).findById(1);
        verify(companyRoleRepo, never()).delete(any(CompanyRole.class));
    }

    @Test
    void getAllPermissions_ShouldReturnAllPermissions() {
        // Arrange
        List<CompanyPermission> permissions = Arrays.asList(testPermission);
        when(companyPermissionRepo.findAll()).thenReturn(permissions);

        // Act
        CompanyRolePermissionGetAllRes result = companyRoleService.getAllPermissions();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getPermissions());
        assertEquals(1, result.getPermissions().size());
        assertEquals("Manage Company Roles", result.getPermissions().get(0).getName());
        verify(companyPermissionRepo, times(1)).findAll();
    }

    @Test
    void createPermission_ShouldCreatePermissionSuccessfully() {
        // Arrange
        CompanyPermissionCreateReq request = new CompanyPermissionCreateReq();
        request.setName("Manage Employees");
        request.setType("EMPLOYEE_MANAGEMENT");
        request.setDescription("Permission to manage employees");

        CompanyPermission savedPermission = new CompanyPermission();
        savedPermission.setId(2);
        savedPermission.setName("Manage Employees");
        savedPermission.setType(PermissionType.EMPLOYEE_MANAGEMENT);
        savedPermission.setDescription("Permission to manage employees");

        when(companyPermissionRepo.save(any(CompanyPermission.class))).thenReturn(savedPermission);

        // Act
        CompanyPermissionCreateRes result = companyRoleService.createPermission(request);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getId());
        assertEquals("Manage Employees", result.getName());
        assertEquals("Permission to manage employees", result.getDescription());
        verify(companyPermissionRepo, times(1)).save(any(CompanyPermission.class));
    }

    @Test
    void updatePermission_ShouldUpdatePermissionSuccessfully() {
        // Arrange
        CompanyPermissionDTO request = new CompanyPermissionDTO(1, "Updated Role Management", "COMPANY_ROLE_MANAGEMENT", "Updated description");

        when(companyPermissionRepo.findById(1)).thenReturn(Optional.of(testPermission));
        when(companyPermissionRepo.save(any(CompanyPermission.class))).thenReturn(testPermission);

        // Act
        CompanyPermissionDTO result = companyRoleService.updatePermission(request);

        // Assert
        assertNotNull(result);
        verify(companyPermissionRepo, times(1)).findById(1);
        verify(companyPermissionRepo, times(1)).save(any(CompanyPermission.class));
    }

    @Test
    void updatePermission_ShouldThrowEntityNotFoundException_WhenPermissionNotFound() {
        // Arrange
        CompanyPermissionDTO request = new CompanyPermissionDTO(999, "Updated Permission", "DONATION_APPROVE", "Updated description");

        when(companyPermissionRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> companyRoleService.updatePermission(request));
        verify(companyPermissionRepo, times(1)).findById(999);
        verify(companyPermissionRepo, never()).save(any(CompanyPermission.class));
    }

    @Test
    void deletePermission_ShouldDeleteSuccessfully() {
        // Arrange
        doNothing().when(companyPermissionRepo).deleteById(1);

        // Act
        Integer result = companyRoleService.deletePermission(1);

        // Assert
        assertEquals(1, result);
        verify(companyPermissionRepo, times(1)).deleteById(1);
    }

    @Test
    void getAllPermissionTypes_ShouldReturnAllTypes() {
        // Act
        PermissionTypeGetAllRes result = companyRoleService.getAllPermissionTypes();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getTypes());
        assertEquals(4, result.getTypes().size());
        assertTrue(result.getTypes().contains("COMPANY_ROLE_MANAGEMENT"));
        assertTrue(result.getTypes().contains("EMPLOYEE_MANAGEMENT"));
        assertTrue(result.getTypes().contains("DONATION_REQUEST"));
        assertTrue(result.getTypes().contains("DONATION_APPROVE"));
    }

    @Test
    void createPermission_ShouldCreateWithCompanyRoleManagementType() {
        // Arrange
        CompanyPermissionCreateReq request = new CompanyPermissionCreateReq();
        request.setName("Role Manager");
        request.setType("COMPANY_ROLE_MANAGEMENT");
        request.setDescription("Can create, update, and delete company roles");

        CompanyPermission savedPermission = new CompanyPermission();
        savedPermission.setId(3);
        savedPermission.setName("Role Manager");
        savedPermission.setType(PermissionType.COMPANY_ROLE_MANAGEMENT);
        savedPermission.setDescription("Can create, update, and delete company roles");

        when(companyPermissionRepo.save(any(CompanyPermission.class))).thenReturn(savedPermission);

        // Act
        CompanyPermissionCreateRes result = companyRoleService.createPermission(request);

        // Assert
        assertNotNull(result);
        assertEquals("Role Manager", result.getName());
        verify(companyPermissionRepo, times(1)).save(any(CompanyPermission.class));
    }

    @Test
    void createPermission_ShouldCreateWithDonationRequestType() {
        // Arrange
        CompanyPermissionCreateReq request = new CompanyPermissionCreateReq();
        request.setName("Request Donations");
        request.setType("DONATION_REQUEST");
        request.setDescription("Can create donation requests");

        CompanyPermission savedPermission = new CompanyPermission();
        savedPermission.setId(4);
        savedPermission.setName("Request Donations");
        savedPermission.setType(PermissionType.DONATION_REQUEST);
        savedPermission.setDescription("Can create donation requests");

        when(companyPermissionRepo.save(any(CompanyPermission.class))).thenReturn(savedPermission);

        // Act
        CompanyPermissionCreateRes result = companyRoleService.createPermission(request);

        // Assert
        assertNotNull(result);
        assertEquals("Request Donations", result.getName());
        verify(companyPermissionRepo, times(1)).save(any(CompanyPermission.class));
    }

    @Test
    void createPermission_ShouldCreateWithDonationApproveType() {
        // Arrange
        CompanyPermissionCreateReq request = new CompanyPermissionCreateReq();
        request.setName("Approve Donations");
        request.setType("DONATION_APPROVE");
        request.setDescription("Can approve or reject donation requests");

        CompanyPermission savedPermission = new CompanyPermission();
        savedPermission.setId(5);
        savedPermission.setName("Approve Donations");
        savedPermission.setType(PermissionType.DONATION_APPROVE);
        savedPermission.setDescription("Can approve or reject donation requests");

        when(companyPermissionRepo.save(any(CompanyPermission.class))).thenReturn(savedPermission);

        // Act
        CompanyPermissionCreateRes result = companyRoleService.createPermission(request);

        // Assert
        assertNotNull(result);
        assertEquals("Approve Donations", result.getName());
        verify(companyPermissionRepo, times(1)).save(any(CompanyPermission.class));
    }

    @Test
    void create_ShouldCreateRoleWithMultiplePermissionTypes() {
        // Arrange
        CompanyPermission employeeMgmt = new CompanyPermission();
        employeeMgmt.setId(2);
        employeeMgmt.setName("Manage Employees");
        employeeMgmt.setType(PermissionType.EMPLOYEE_MANAGEMENT);

        CompanyPermission donationRequest = new CompanyPermission();
        donationRequest.setId(3);
        donationRequest.setName("Request Donations");
        donationRequest.setType(PermissionType.DONATION_REQUEST);

        CompanyRoleCreateReq request = new CompanyRoleCreateReq();
        request.setName("Department Manager");
        request.setCompanyId(1);
        request.setPermissionIds(Arrays.asList(1, 2, 3)); // Role management, employee management, donation request

        when(companyRoleRepo.findByCompanyIdAndName(1, "Department Manager")).thenReturn(null);
        when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
        when(companyPermissionRepo.findAllById(anyList())).thenReturn(Arrays.asList(testPermission, employeeMgmt, donationRequest));
        when(companyRoleRepo.save(any(CompanyRole.class))).thenReturn(testRole);
        when(roleMapper.toDTO(any(CompanyRole.class))).thenReturn(testRoleDTO);

        // Act
        CompanyRoleDTO result = companyRoleService.create(request);

        // Assert
        assertNotNull(result);
        verify(companyPermissionRepo, times(1)).findAllById(Arrays.asList(1, 2, 3));
        verify(companyRoleRepo, times(1)).save(any(CompanyRole.class));
    }
}