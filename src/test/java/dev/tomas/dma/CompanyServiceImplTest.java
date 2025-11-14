package dev.tomas.dma;

import dev.tomas.dma.dto.common.CompanyDTO;
import dev.tomas.dma.dto.common.CompanyTypeDTO;
import dev.tomas.dma.dto.request.CompanyCreateReq;
import dev.tomas.dma.dto.request.CompanyTypeCreateReq;
import dev.tomas.dma.dto.response.CompanyGetAllRes;
import dev.tomas.dma.dto.response.CompanyTypeGetAllRes;
import dev.tomas.dma.dto.response.CompanyTypeGetRes;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.entity.CompanyRole;
import dev.tomas.dma.entity.CompanyType;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.mapper.CompanyMapper;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.repository.CompanyRoleRepo;
import dev.tomas.dma.repository.CompanyTypeRepo;
import dev.tomas.dma.repository.UserRepo;
import dev.tomas.dma.service.CompanyRoleService;
import dev.tomas.dma.service.MediaService;
import dev.tomas.dma.service.implementation.CompanyServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTest {

    @Mock
    private CompanyRepo companyRepo;

    @Mock
    private CompanyTypeRepo companyTypeRepo;

    @Mock
    private CompanyRoleRepo companyRoleRepo;

    @Mock
    private CompanyRoleService roleService;

    @Mock
    private MediaService mediaService;

    @Mock
    private UserRepo userRepo;

    @Mock
    private CompanyMapper companyMapper;

    @InjectMocks
    private CompanyServiceImpl companyService;

    private Company testCompany;
    private CompanyType testCompanyType;
    private User testUser;
    private CompanyDTO testCompanyDTO;
    private CompanyTypeDTO testCompanyTypeDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        testCompanyType = new CompanyType();
        testCompanyType.setId(1);
        testCompanyType.setName("Tech Company");
        testCompanyType.setDescription("Technology companies");

        testCompany = new Company();
        testCompany.setId(1);
        testCompany.setName("Test Company");
        testCompany.setRegistrationNumber("REG123");
        testCompany.setTaxId("TAX123");
        testCompany.setType(testCompanyType);

        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@test.com");
        testUser.setUsername("testuser");

        testCompanyTypeDTO = new CompanyTypeDTO(1, "Tech Company", "Technology companies");
        testCompanyDTO = new CompanyDTO(1, "Test Company", "REG123", "TAX123", testCompanyTypeDTO);
    }

    @Test
    void getAll_ShouldReturnAllCompanies() {
        // Arrange
        List<Company> companies = Arrays.asList(testCompany);
        when(companyRepo.findAll()).thenReturn(companies);

        // Act
        CompanyGetAllRes result = companyService.getAll();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCompanies());
        assertEquals(1, result.getCompanies().size());
        assertEquals("Test Company", result.getCompanies().get(0).getName());
        verify(companyRepo, times(1)).findAll();
    }

    @Test
    void getAll_ShouldReturnEmptyListWhenNoCompanies() {
        // Arrange
        when(companyRepo.findAll()).thenReturn(new ArrayList<>());

        // Act
        CompanyGetAllRes result = companyService.getAll();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCompanies());
        assertTrue(result.getCompanies().isEmpty());
        verify(companyRepo, times(1)).findAll();
    }

    @Test
    void getById_ShouldReturnCompanyDTO_WhenCompanyExists() {
        // Arrange
        when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
        when(companyMapper.toDto(testCompany)).thenReturn(testCompanyDTO);

        // Act
        CompanyDTO result = companyService.getById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Company", result.getName());
        verify(companyRepo, times(1)).findById(1);
        verify(companyMapper, times(1)).toDto(testCompany);
    }

    @Test
    void getById_ShouldThrowEntityNotFoundException_WhenCompanyNotFound() {
        // Arrange
        when(companyRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> companyService.getById(999));
        verify(companyRepo, times(1)).findById(999);
    }

    @Test
    void save_ShouldCreateCompanyWithDefaultRoles() {
        // Arrange
        CompanyCreateReq request = new CompanyCreateReq();
        request.setName("New Company");
        request.setRegistrationNumber("REG456");
        request.setTaxId("TAX456");
        request.setTypeId(1);
        request.setUserId(1);

        CompanyRole ownerRole = new CompanyRole();
        ownerRole.setId(1);
        ownerRole.setName("Owner");
        ownerRole.setCompany(testCompany);

        CompanyRole employeeRole = new CompanyRole();
        employeeRole.setId(2);
        employeeRole.setName("Employee");
        employeeRole.setCompany(testCompany);

        List<CompanyRole> roles = Arrays.asList(employeeRole, ownerRole);

        when(companyTypeRepo.findById(1)).thenReturn(Optional.of(testCompanyType));
        when(userRepo.findById(1)).thenReturn(Optional.of(testUser));
        when(companyRepo.save(any(Company.class))).thenReturn(testCompany);
        when(companyRoleRepo.save(any(CompanyRole.class)))
                .thenReturn(employeeRole)
                .thenReturn(ownerRole);
        when(roleService.getAllPermissionsEntity()).thenReturn(new ArrayList<>());
        when(userRepo.save(any(User.class))).thenReturn(testUser);
        when(companyMapper.toDto(testCompany)).thenReturn(testCompanyDTO);
        doNothing().when(mediaService).createFolder(anyString());

        // Act
        CompanyDTO result = companyService.save(request);

        // Assert
        assertNotNull(result);
        verify(companyTypeRepo, times(1)).findById(1);
        verify(userRepo, times(1)).findById(1);
        verify(companyRepo, times(1)).save(any(Company.class));
        verify(mediaService, times(1)).createFolder(anyString());
        verify(companyRoleRepo, times(2)).save(any(CompanyRole.class));
        verify(userRepo, times(1)).save(testUser);
    }

    @Test
    void save_ShouldThrowEntityNotFoundException_WhenTypeNotFound() {
        // Arrange
        CompanyCreateReq request = new CompanyCreateReq();
        request.setTypeId(999);
        request.setUserId(1);

        when(companyTypeRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> companyService.save(request));
        verify(companyTypeRepo, times(1)).findById(999);
        verify(companyRepo, never()).save(any(Company.class));
    }

    @Test
    void save_ShouldThrowEntityNotFoundException_WhenUserNotFound() {
        // Arrange
        CompanyCreateReq request = new CompanyCreateReq();
        request.setTypeId(1);
        request.setUserId(999);

        when(companyTypeRepo.findById(1)).thenReturn(Optional.of(testCompanyType));
        when(userRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> companyService.save(request));
        verify(companyTypeRepo, times(1)).findById(1);
        verify(userRepo, times(1)).findById(999);
        verify(companyRepo, never()).save(any(Company.class));
    }

    @Test
    void getAllTypes_ShouldReturnAllCompanyTypes() {
        // Arrange
        List<CompanyType> types = Arrays.asList(testCompanyType);
        when(companyTypeRepo.findAll()).thenReturn(types);

        // Act
        CompanyTypeGetAllRes result = companyService.getAllTypes();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getTypes());
        assertEquals(1, result.getTypes().size());
        assertEquals("Tech Company", result.getTypes().get(0).getName());
        verify(companyTypeRepo, times(1)).findAll();
    }

    @Test
    void getTypeById_ShouldReturnCompanyType_WhenExists() {
        // Arrange
        when(companyTypeRepo.findById(1)).thenReturn(Optional.of(testCompanyType));

        // Act
        CompanyTypeGetRes result = companyService.getTypeById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Tech Company", result.getName());
        assertEquals("Technology companies", result.getDescription());
        verify(companyTypeRepo, times(1)).findById(1);
    }

    @Test
    void getTypeById_ShouldThrowEntityNotFoundException_WhenNotFound() {
        // Arrange
        when(companyTypeRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> companyService.getTypeById(999));
        verify(companyTypeRepo, times(1)).findById(999);
    }

    @Test
    void saveType_ShouldSaveAndReturnCompanyType() {
        // Arrange
        CompanyTypeCreateReq request = new CompanyTypeCreateReq();
        request.setName("New Type");
        request.setDescription("New Description");

        CompanyType savedType = new CompanyType();
        savedType.setId(2);
        savedType.setName("New Type");
        savedType.setDescription("New Description");

        when(companyTypeRepo.save(any(CompanyType.class))).thenReturn(savedType);

        // Act
        CompanyTypeGetRes result = companyService.saveType(request);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getId());
        assertEquals("New Type", result.getName());
        assertEquals("New Description", result.getDescription());
        verify(companyTypeRepo, times(1)).save(any(CompanyType.class));
    }

    @Test
    void saveType_ShouldThrowException_WhenNameIsBlank() {
        // Arrange
        CompanyTypeCreateReq request = new CompanyTypeCreateReq();
        request.setName("");
        request.setDescription("Description");

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> companyService.saveType(request));
        verify(companyTypeRepo, never()).save(any(CompanyType.class));
    }

    @Test
    void saveType_ShouldThrowException_WhenDescriptionIsBlank() {
        // Arrange
        CompanyTypeCreateReq request = new CompanyTypeCreateReq();
        request.setName("Name");
        request.setDescription("");

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> companyService.saveType(request));
        verify(companyTypeRepo, never()).save(any(CompanyType.class));
    }

    @Test
    void updateType_ShouldUpdateAndReturnCompanyType() {
        // Arrange
        CompanyTypeDTO request = new CompanyTypeDTO(1, "Updated Type", "Updated Description");

        when(companyTypeRepo.findById(1)).thenReturn(Optional.of(testCompanyType));
        when(companyTypeRepo.save(any(CompanyType.class))).thenReturn(testCompanyType);

        // Act
        CompanyTypeDTO result = companyService.updateType(request);

        // Assert
        assertNotNull(result);
        verify(companyTypeRepo, times(1)).findById(1);
        verify(companyTypeRepo, times(1)).save(any(CompanyType.class));
    }

    @Test
    void updateType_ShouldThrowException_WhenNameIsBlank() {
        // Arrange
        CompanyTypeDTO request = new CompanyTypeDTO(1, "", "Description");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> companyService.updateType(request));
        verify(companyTypeRepo, never()).findById(any());
    }

    @Test
    void updateType_ShouldThrowException_WhenDescriptionIsBlank() {
        // Arrange
        CompanyTypeDTO request = new CompanyTypeDTO(1, "Name", "");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> companyService.updateType(request));
        verify(companyTypeRepo, never()).findById(any());
    }

    @Test
    void updateType_ShouldThrowEntityNotFoundException_WhenTypeNotFound() {
        // Arrange
        CompanyTypeDTO request = new CompanyTypeDTO(999, "Name", "Description");

        when(companyTypeRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> companyService.updateType(request));
        verify(companyTypeRepo, times(1)).findById(999);
        verify(companyTypeRepo, never()).save(any());
    }

    @Test
    void deleteType_ShouldDeleteAndReturnId() {
        // Arrange
        doNothing().when(companyTypeRepo).deleteById(1);

        // Act
        Integer result = companyService.deleteType(1);

        // Assert
        assertEquals(1, result);
        verify(companyTypeRepo, times(1)).deleteById(1);
    }

    @Test
    void findCompanyEntityById_ShouldReturnCompany_WhenExists() {
        // Arrange
        when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));

        // Act
        Company result = companyService.findCompanyEntityById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Company", result.getName());
        verify(companyRepo, times(1)).findById(1);
    }

    @Test
    void findCompanyEntityById_ShouldThrowEntityNotFoundException_WhenNotFound() {
        // Arrange
        when(companyRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> companyService.findCompanyEntityById(999));
        verify(companyRepo, times(1)).findById(999);
    }

    @Test
    void createDefaultRoles_ShouldCreateEmployeeAndOwnerRoles() {
        // Arrange
        CompanyRole employeeRole = new CompanyRole();
        employeeRole.setId(1);
        employeeRole.setName("Employee");
        employeeRole.setCompany(testCompany);

        CompanyRole ownerRole = new CompanyRole();
        ownerRole.setId(2);
        ownerRole.setName("Owner");
        ownerRole.setCompany(testCompany);

        when(companyRoleRepo.save(any(CompanyRole.class)))
                .thenReturn(employeeRole)
                .thenReturn(ownerRole);
        when(roleService.getAllPermissionsEntity()).thenReturn(new ArrayList<>());

        // Act
        List<CompanyRole> result = companyService.createDefaultRoles(testCompany);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Employee", result.get(0).getName());
        assertEquals("Owner", result.get(1).getName());
        verify(companyRoleRepo, times(2)).save(any(CompanyRole.class));
        verify(roleService, times(1)).getAllPermissionsEntity();
    }
}