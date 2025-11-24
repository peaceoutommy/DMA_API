package dev.tomas.dma.IntegrationTest;

import dev.tomas.dma.dto.request.CompanyCreateReq;
import dev.tomas.dma.dto.request.CompanyTypeCreateReq;
import dev.tomas.dma.dto.common.CompanyTypeDTO;
import dev.tomas.dma.entity.*;
import dev.tomas.dma.enums.UserRole;
import dev.tomas.dma.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CompanyControllerIntegrationTest extends BaseIntegrationTest {

    @MockBean
    private FileService fileService; // Mock external service

    private CompanyType testCompanyType;
    private User testUser;

    @BeforeEach
    void setUpTest() {
        // Note: BaseIntegrationTest.setUp() already runs and cleans DB

        // Mock MediaService to avoid external dependencies
        doNothing().when(fileService).createFolder(anyString());

        // Create test company type
        testCompanyType = new CompanyType();
        testCompanyType.setName("Tech Startup");
        testCompanyType.setDescription("Technology startup company");
        testCompanyType = companyTypeRepo.save(testCompanyType);

        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setPhoneNumber("+1234567890");
        testUser.setAddress("123 Test St");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setUsername("johndoe");
        testUser.setRole(UserRole.DONOR);
        testUser.setEnabled(true);
        testUser = userRepo.save(testUser);
    }

    // ==================== Company CRUD Tests ====================

    @Test
    @WithMockUser
    void shouldGetAllCompanies() throws Exception {
        // Given: Create test companies
        createTestCompany("Test Company 1", "REG123", "TAX123");
        createTestCompany("Test Company 2", "REG456", "TAX456");

        // When & Then
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companies", hasSize(2)))
                .andExpect(jsonPath("$.companies[0].name").exists())
                .andExpect(jsonPath("$.companies[0].registrationNumber").exists())
                .andExpect(jsonPath("$.companies[0].taxId").exists())
                .andExpect(jsonPath("$.companies[0].type").exists());
    }

    @Test
    @WithMockUser
    void shouldGetEmptyListWhenNoCompanies() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companies", hasSize(0)));
    }

    @Test
    @WithMockUser
    void shouldGetCompanyById() throws Exception {
        // Given
        Company company = createTestCompany("Tech Corp", "REG789", "TAX789");

        // When & Then
        mockMvc.perform(get("/api/companies/{id}", company.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(company.getId()))
                .andExpect(jsonPath("$.name").value("Tech Corp"))
                .andExpect(jsonPath("$.registrationNumber").value("REG789"))
                .andExpect(jsonPath("$.taxId").value("TAX789"))
                .andExpect(jsonPath("$.type.id").value(testCompanyType.getId()))
                .andExpect(jsonPath("$.type.name").value(testCompanyType.getName()));
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenCompanyNotFound() throws Exception {
        mockMvc.perform(get("/api/companies/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldCreateCompanySuccessfully() throws Exception {
        // Given
        CompanyCreateReq request = new CompanyCreateReq();
        request.setName("New Company");
        request.setRegistrationNumber("NEWREG123");
        request.setTaxId("NEWTAX123");
        request.setTypeId(testCompanyType.getId());
        request.setUserId(testUser.getId());

        // When & Then
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Company"))
                .andExpect(jsonPath("$.registrationNumber").value("NEWREG123"))
                .andExpect(jsonPath("$.taxId").value("NEWTAX123"))
                .andExpect(jsonPath("$.type.id").value(testCompanyType.getId()));

        // Verify database state
        assertEquals(1, companyRepo.count());
        Company savedCompany = companyRepo.findAll().get(0);
        assertEquals("New Company", savedCompany.getName());

        // Verify default roles were created (Owner and Employee)
        assertEquals(2, companyRoleRepo.findAllByCompanyId(savedCompany.getId()).size());

        // Verify user was assigned to company with owner role
        User updatedUser = userRepo.findById(testUser.getId()).orElseThrow();
        assertNotNull(updatedUser.getCompany());
        assertEquals(savedCompany.getId(), updatedUser.getCompany().getId());
        assertNotNull(updatedUser.getCompanyRole());
        assertEquals("Owner", updatedUser.getCompanyRole().getName());

        // Verify MediaService was called
        verify(fileService, times(1)).createFolder(savedCompany.getId().toString());
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenCreatingCompanyWithInvalidData() throws Exception {
        // Given: Invalid request (name too short)
        CompanyCreateReq request = new CompanyCreateReq();
        request.setName("AB"); // Too short (min 3)
        request.setRegistrationNumber("REG123");
        request.setTaxId("TAX12345");
        request.setTypeId(testCompanyType.getId());
        request.setUserId(testUser.getId());

        // When & Then
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenCreatingCompanyWithNonExistentType() throws Exception {
        // Given
        CompanyCreateReq request = new CompanyCreateReq();
        request.setName("Test Company");
        request.setRegistrationNumber("REG12345");
        request.setTaxId("TAX123456");
        request.setTypeId(99999); // Non-existent type
        request.setUserId(testUser.getId());

        // When & Then
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenCreatingCompanyWithNonExistentUser() throws Exception {
        // Given
        CompanyCreateReq request = new CompanyCreateReq();
        request.setName("Test Company");
        request.setRegistrationNumber("REG12345");
        request.setTaxId("TAX123456");
        request.setTypeId(testCompanyType.getId());
        request.setUserId(99999); // Non-existent user

        // When & Then
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ==================== Company Type Tests ====================

    @Test
    @WithMockUser
    void shouldGetAllCompanyTypes() throws Exception {
        // Given: Additional company type
        CompanyType type2 = new CompanyType();
        type2.setName("Non-Profit");
        type2.setDescription("Non-profit organization");
        companyTypeRepo.save(type2);

        // When & Then
        mockMvc.perform(get("/api/companies/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.types", hasSize(2)))
                .andExpect(jsonPath("$.types[0].name").exists())
                .andExpect(jsonPath("$.types[0].description").exists());
    }

    @Test
    @WithMockUser
    void shouldGetCompanyTypeById() throws Exception {
        mockMvc.perform(get("/api/companies/types/{id}", testCompanyType.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCompanyType.getId()))
                .andExpect(jsonPath("$.name").value("Tech Startup"))
                .andExpect(jsonPath("$.description").value("Technology startup company"));
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenCompanyTypeNotFound() throws Exception {
        mockMvc.perform(get("/api/companies/types/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldCreateCompanyType() throws Exception {
        // Given
        CompanyTypeCreateReq request = new CompanyTypeCreateReq();
        request.setName("E-Commerce");
        request.setDescription("E-commerce and retail business");

        // When & Then
        mockMvc.perform(post("/api/companies/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("E-Commerce"))
                .andExpect(jsonPath("$.description").value("E-commerce and retail business"));

        // Verify database
        assertEquals(2, companyTypeRepo.count());
    }

    @Test
    @WithMockUser
    void shouldReturn400WhenCreatingCompanyTypeWithEmptyName() throws Exception {
        // Given
        CompanyTypeCreateReq request = new CompanyTypeCreateReq();
        request.setName("");
        request.setDescription("Valid description");

        // When & Then
        mockMvc.perform(post("/api/companies/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void shouldUpdateCompanyType() throws Exception {
        // Given
        CompanyTypeDTO updateRequest = new CompanyTypeDTO(
                testCompanyType.getId(),
                "Updated Tech Startup",
                "Updated description for tech startups"
        );

        // When & Then
        mockMvc.perform(put("/api/companies/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCompanyType.getId()))
                .andExpect(jsonPath("$.name").value("Updated Tech Startup"))
                .andExpect(jsonPath("$.description").value("Updated description for tech startups"));

        // Verify database
        CompanyType updated = companyTypeRepo.findById(testCompanyType.getId()).orElseThrow();
        assertEquals("Updated Tech Startup", updated.getName());
    }

    @Test
    @WithMockUser
    void shouldReturn404WhenUpdatingNonExistentCompanyType() throws Exception {
        // Given
        CompanyTypeDTO updateRequest = new CompanyTypeDTO(
                99999,
                "Non Existent",
                "This should fail"
        );

        // When & Then
        mockMvc.perform(put("/api/companies/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void shouldDeleteCompanyType() throws Exception {
        // Given: Create a type that's not being used
        CompanyType unusedType = new CompanyType();
        unusedType.setName("Unused Type");
        unusedType.setDescription("This type is not used");
        unusedType = companyTypeRepo.save(unusedType);

        // When & Then
        mockMvc.perform(delete("/api/companies/types/{id}", unusedType.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(unusedType.getId().toString()));

        // Verify deletion
        assertFalse(companyTypeRepo.existsById(unusedType.getId()));
    }

    // ==================== Helper Methods ====================

    private Company createTestCompany(String name, String regNumber, String taxId) {
        Company company = new Company();
        company.setName(name);
        company.setRegistrationNumber(regNumber);
        company.setTaxId(taxId);
        company.setType(testCompanyType);
        return companyRepo.save(company);
    }
}