package dev.tomas.dma.service.implementation;

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
import dev.tomas.dma.service.ExternalStorageService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    private ExternalStorageService externalStorageService;

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
    private CompanyCreateReq createRequest;
    private CompanyTypeCreateReq typeCreateRequest;

    @BeforeEach
    void setUp() {
        testCompanyType = new CompanyType();
        testCompanyType.setId(1);
        testCompanyType.setName("Non-Profit");
        testCompanyType.setDescription("Non-profit organization");

        testCompany = new Company();
        testCompany.setId(1);
        testCompany.setName("Test Company");
        testCompany.setRegistrationNumber("REG123456");
        testCompany.setTaxId("TAX12345678");
        testCompany.setType(testCompanyType);

        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("owner@example.com");

        CompanyTypeDTO typeDTO = new CompanyTypeDTO(1, "Non-Profit", "Non-profit organization");
        testCompanyDTO = new CompanyDTO(1, "Test Company", "REG123456", "TAX12345678", typeDTO, "APPROVED");

        createRequest = new CompanyCreateReq();
        createRequest.setUserId(1);
        createRequest.setName("New Company");
        createRequest.setRegistrationNumber("REG789012");
        createRequest.setTaxId("TAX87654321");
        createRequest.setTypeId(1);

        typeCreateRequest = new CompanyTypeCreateReq();
        typeCreateRequest.setName("Charity");
        typeCreateRequest.setDescription("Charitable organization");
    }

    @Nested
    @DisplayName("GetAll Tests")
    class GetAllTests {

        @Test
        @DisplayName("Should return all companies")
        void getAll_Success() {
            when(companyRepo.findAll()).thenReturn(Arrays.asList(testCompany));

            CompanyGetAllRes result = companyService.getAll();

            assertThat(result).isNotNull();
            assertThat(result.getCompanies()).hasSize(1);
            assertThat(result.getCompanies().get(0).getName()).isEqualTo("Test Company");
        }

        @Test
        @DisplayName("Should return empty list when no companies exist")
        void getAll_EmptyList() {
            when(companyRepo.findAll()).thenReturn(new ArrayList<>());

            CompanyGetAllRes result = companyService.getAll();

            assertThat(result.getCompanies()).isEmpty();
        }
    }

    @Nested
    @DisplayName("GetById Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return company by id")
        void getById_Success() {
            when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
            when(companyMapper.toDto(testCompany)).thenReturn(testCompanyDTO);

            CompanyDTO result = companyService.getById(1);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Test Company");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when company not found")
        void getById_ThrowsException_WhenNotFound() {
            when(companyRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyService.getById(999))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Company not found with id");
        }
    }

    @Nested
    @DisplayName("Save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save company and create default roles")
        void save_Success() {
            CompanyRole ownerRole = new CompanyRole();
            ownerRole.setId(1);
            ownerRole.setName("Owner");

            CompanyRole employeeRole = new CompanyRole();
            employeeRole.setId(2);
            employeeRole.setName("Employee");

            when(companyTypeRepo.findById(1)).thenReturn(Optional.of(testCompanyType));
            when(userRepo.findById(1)).thenReturn(Optional.of(testUser));
            when(companyRepo.save(any(Company.class))).thenAnswer(invocation -> {
                Company company = invocation.getArgument(0);
                company.setId(1);
                return company;
            });
            when(companyRoleRepo.save(any(CompanyRole.class))).thenAnswer(invocation -> {
                CompanyRole role = invocation.getArgument(0);
                if ("Owner".equals(role.getName())) {
                    role.setId(1);
                } else {
                    role.setId(2);
                }
                return role;
            });
            when(roleService.getAllPermissionsEntity()).thenReturn(new ArrayList<>());
            when(userRepo.save(any(User.class))).thenReturn(testUser);
            when(companyMapper.toDto(any(Company.class))).thenReturn(testCompanyDTO);

            CompanyDTO result = companyService.save(createRequest);

            assertThat(result).isNotNull();
            verify(companyRepo).save(any(Company.class));
            verify(externalStorageService).createFolder(anyString());
            verify(companyRoleRepo, times(2)).save(any(CompanyRole.class));
            verify(userRepo).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when company type not found")
        void save_ThrowsException_WhenTypeNotFound() {
            when(companyTypeRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyService.save(createRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Type not found with id: 1");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when user not found")
        void save_ThrowsException_WhenUserNotFound() {
            when(companyTypeRepo.findById(1)).thenReturn(Optional.of(testCompanyType));
            when(userRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyService.save(createRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User not found with id: 1");
        }
    }

    @Nested
    @DisplayName("Company Type Tests")
    class CompanyTypeTests {

        @Test
        @DisplayName("Should return all company types")
        void getAllTypes_Success() {
            when(companyTypeRepo.findAll()).thenReturn(Arrays.asList(testCompanyType));

            CompanyTypeGetAllRes result = companyService.getAllTypes();

            assertThat(result).isNotNull();
            assertThat(result.getTypes()).hasSize(1);
            assertThat(result.getTypes().get(0).getName()).isEqualTo("Non-Profit");
        }

        @Test
        @DisplayName("Should return company type by id")
        void getTypeById_Success() {
            when(companyTypeRepo.findById(1)).thenReturn(Optional.of(testCompanyType));

            CompanyTypeGetRes result = companyService.getTypeById(1);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getName()).isEqualTo("Non-Profit");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when type not found")
        void getTypeById_ThrowsException_WhenNotFound() {
            when(companyTypeRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyService.getTypeById(999))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Company type not found with id: 999");
        }

        @Test
        @DisplayName("Should save company type successfully")
        void saveType_Success() {
            when(companyTypeRepo.save(any(CompanyType.class))).thenAnswer(invocation -> {
                CompanyType type = invocation.getArgument(0);
                type.setId(2);
                return type;
            });

            CompanyTypeGetRes result = companyService.saveType(typeCreateRequest);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Charity");
            assertThat(result.getDescription()).isEqualTo("Charitable organization");
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when type name is blank")
        void saveType_ThrowsException_WhenNameBlank() {
            typeCreateRequest.setName("");

            assertThatThrownBy(() -> companyService.saveType(typeCreateRequest))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting("statusCode")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when type description is blank")
        void saveType_ThrowsException_WhenDescriptionBlank() {
            typeCreateRequest.setDescription("");

            assertThatThrownBy(() -> companyService.saveType(typeCreateRequest))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting("statusCode")
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Should update company type successfully")
        void updateType_Success() {
            CompanyTypeDTO updateRequest = new CompanyTypeDTO(1, "Updated Type", "Updated description");

            when(companyTypeRepo.findById(1)).thenReturn(Optional.of(testCompanyType));
            when(companyTypeRepo.save(any(CompanyType.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CompanyTypeDTO result = companyService.updateType(updateRequest);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Updated Type");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when updating with blank name")
        void updateType_ThrowsException_WhenNameBlank() {
            CompanyTypeDTO updateRequest = new CompanyTypeDTO(1, "", "Description");

            assertThatThrownBy(() -> companyService.updateType(updateRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Company type name can't be empty");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when updating with blank description")
        void updateType_ThrowsException_WhenDescriptionBlank() {
            CompanyTypeDTO updateRequest = new CompanyTypeDTO(1, "Name", "");

            assertThatThrownBy(() -> companyService.updateType(updateRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Company type description can't be empty");
        }

        @Test
        @DisplayName("Should delete company type successfully")
        void deleteType_Success() {
            doNothing().when(companyTypeRepo).deleteById(1);

            Integer result = companyService.deleteType(1);

            assertThat(result).isEqualTo(1);
            verify(companyTypeRepo).deleteById(1);
        }
    }

    @Nested
    @DisplayName("FindCompanyEntityById Tests")
    class FindCompanyEntityByIdTests {

        @Test
        @DisplayName("Should return company entity by id")
        void findCompanyEntityById_Success() {
            when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));

            Company result = companyService.findCompanyEntityById(1);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when not found")
        void findCompanyEntityById_ThrowsException_WhenNotFound() {
            when(companyRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyService.findCompanyEntityById(999))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Company not found with id: 999");
        }
    }

    @Nested
    @DisplayName("CreateDefaultRoles Tests")
    class CreateDefaultRolesTests {

        @Test
        @DisplayName("Should create Employee and Owner roles")
        void createDefaultRoles_Success() {
            when(companyRoleRepo.save(any(CompanyRole.class))).thenAnswer(invocation -> {
                CompanyRole role = invocation.getArgument(0);
                if ("Owner".equals(role.getName())) {
                    role.setId(1);
                } else {
                    role.setId(2);
                }
                return role;
            });
            when(roleService.getAllPermissionsEntity()).thenReturn(new ArrayList<>());

            List<CompanyRole> result = companyService.createDefaultRoles(testCompany);

            assertThat(result).hasSize(2);
            assertThat(result).extracting(CompanyRole::getName).containsExactlyInAnyOrder("Employee", "Owner");
            verify(companyRoleRepo, times(2)).save(any(CompanyRole.class));
        }
    }
}
