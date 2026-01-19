package dev.tomas.dma.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tomas.dma.dto.common.CompanyTypeDTO;
import dev.tomas.dma.dto.request.CompanyCreateReq;
import dev.tomas.dma.dto.request.CompanyTypeCreateReq;
import dev.tomas.dma.dto.request.FundRequestCreateReq;
import dev.tomas.dma.entity.*;
import dev.tomas.dma.enums.CampaignStatus;
import dev.tomas.dma.enums.CompanyStatus;
import dev.tomas.dma.enums.UserRole;
import dev.tomas.dma.repository.*;
import dev.tomas.dma.service.ExternalStorageService;
import dev.tomas.dma.service.TicketService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CompanyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepo companyRepo;

    @Autowired
    private CompanyTypeRepo companyTypeRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CampaignRepo campaignRepo;

    @Autowired
    private FundRequestRepo fundRequestRepo;

    @MockitoBean
    private ExternalStorageService storageService;

    @MockitoBean
    private TicketService ticketService;

    private CompanyType testType;
    private Company testCompany;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean up in reverse order of dependencies
        fundRequestRepo.deleteAll();
        campaignRepo.deleteAll();

        // Remove user-company associations before deleting companies
        List<User> usersWithCompany = userRepo.findAll().stream()
                .filter(u -> u.getCompany() != null)
                .toList();
        usersWithCompany.forEach(u -> {
            u.setCompany(null);
            u.setCompanyRole(null);
            userRepo.save(u);
        });

        companyRepo.deleteAll();
        companyTypeRepo.deleteAll();

        // Delete test users created by this test
        userRepo.findAll().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().startsWith("testuser"))
                .forEach(userRepo::delete);

        // Create test CompanyType
        CompanyType type = new CompanyType();
        type.setName("Test Type " + System.currentTimeMillis());
        type.setCreateDate(LocalDate.now());
        type.setDescription("Test type description for integration tests");
        testType = companyTypeRepo.save(type);

        // Create test User
        User user = new User();
        user.setEmail("testuser" + System.currentTimeMillis() + "@test.com");
        user.setPassword("password123");
        user.setPhoneNumber("+1234567890" + (System.currentTimeMillis() % 1000));
        user.setAddress("123 Test Street");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setUsername("testuser" + System.currentTimeMillis());
        user.setRole(UserRole.DONOR);
        user.setEnabled(true);
        testUser = userRepo.save(user);

        // Create test Company
        Company company = new Company();
        company.setName("Test Company " + System.currentTimeMillis());
        company.setStatus(CompanyStatus.APPROVED);
        company.setRegistrationNumber("REG" + System.currentTimeMillis());
        company.setCreateDate(LocalDate.now());
        company.setTaxId("TAX" + System.currentTimeMillis());
        company.setType(testType);
        testCompany = companyRepo.save(company);
    }

    @AfterEach
    void tearDown() {
        // Clean up test data after each test
        fundRequestRepo.deleteAll();
        campaignRepo.deleteAll();

        // Remove user-company associations
        List<User> usersWithCompany = userRepo.findAll().stream()
                .filter(u -> u.getCompany() != null)
                .toList();
        usersWithCompany.forEach(u -> {
            u.setCompany(null);
            u.setCompanyRole(null);
            userRepo.save(u);
        });

        companyRepo.deleteAll();
        companyTypeRepo.deleteAll();

        // Delete test users
        userRepo.findAll().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().startsWith("testuser"))
                .forEach(userRepo::delete);
    }

    //  Company Endpoints 

    @Test
    @WithMockUser
    @DisplayName("GET /api/companies - Should return all companies")
    void getAll_ShouldReturnAllCompanies() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companies", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.companies[0].name", notNullValue()));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/companies/{id} - Should return company by ID")
    void getById_ShouldReturnCompany() throws Exception {
        mockMvc.perform(get("/api/companies/{id}", testCompany.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testCompany.getId())))
                .andExpect(jsonPath("$.name", is(testCompany.getName())))
                .andExpect(jsonPath("$.registrationNumber", is(testCompany.getRegistrationNumber())))
                .andExpect(jsonPath("$.taxId", is(testCompany.getTaxId())));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/companies/{id} - Should return 404 for non-existent company")
    void getById_NonExistent_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/companies/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/companies - Should create new company")
    void create_ShouldReturnSavedCompany() throws Exception {
        CompanyCreateReq request = new CompanyCreateReq(
                testUser.getId(),
                "New Company " + System.currentTimeMillis(),
                "84127641249",
                "NL123481721",
                testType.getId()
        );

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(request.getName())))
                .andExpect(jsonPath("$.registrationNumber", is(request.getRegistrationNumber())))
                .andExpect(jsonPath("$.taxId", is(request.getTaxId())));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/companies - Should fail with invalid request (missing name)")
    void create_WithInvalidRequest_ShouldReturn400() throws Exception {
        CompanyCreateReq request = new CompanyCreateReq(
                testUser.getId(),
                "", // Invalid: blank name
                "REG123456",
                "TAX12345678",
                testType.getId()
        );

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/companies - Should fail with invalid request (name too short)")
    void create_WithShortName_ShouldReturn400() throws Exception {
        CompanyCreateReq request = new CompanyCreateReq(
                testUser.getId(),
                "AB", // Invalid: less than 3 characters
                "REG123456",
                "TAX12345678",
                testType.getId()
        );

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/companies - Should fail with null userId")
    void create_WithNullUserId_ShouldReturn400() throws Exception {
        CompanyCreateReq request = new CompanyCreateReq(
                null, // Invalid: null userId
                "Valid Company Name",
                "REG123456",
                "TAX12345678",
                testType.getId()
        );

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    //  CompanyType Endpoints 

    @Test
    @WithMockUser
    @DisplayName("GET /api/companies/types - Should return all company types")
    void getAllTypes_ShouldReturnAllTypes() throws Exception {
        mockMvc.perform(get("/api/companies/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyTypes", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.companyTypes[0].name", notNullValue()));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/companies/types/{id} - Should return company type by ID")
    void getTypeById_ShouldReturnType() throws Exception {
        mockMvc.perform(get("/api/companies/types/{id}", testType.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testType.getId())))
                .andExpect(jsonPath("$.name", is(testType.getName())))
                .andExpect(jsonPath("$.description", is(testType.getDescription())));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/companies/types/{id} - Should return 404 for non-existent type")
    void getTypeById_NonExistent_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/companies/types/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/companies/types - Should create new company type")
    void createType_ShouldReturnSavedType() throws Exception {
        CompanyTypeCreateReq request = new CompanyTypeCreateReq(
                "New Type " + System.currentTimeMillis(),
                "Description for the new company type"
        );

        mockMvc.perform(post("/api/companies/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(request.getName())))
                .andExpect(jsonPath("$.description", is(request.getDescription())));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/companies/types - Should fail with blank name")
    void createType_WithBlankName_ShouldReturn400() throws Exception {
        CompanyTypeCreateReq request = new CompanyTypeCreateReq(
                "", // Invalid: blank name
                "Valid description"
        );

        mockMvc.perform(post("/api/companies/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/companies/types - Should update existing company type")
    void updateType_ShouldReturnUpdatedType() throws Exception {
        CompanyTypeDTO updateRequest = new CompanyTypeDTO(
                testType.getId(),
                "Updated Type Name",
                "Updated description"
        );

        mockMvc.perform(put("/api/companies/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testType.getId())))
                .andExpect(jsonPath("$.name", is("Updated Type Name")))
                .andExpect(jsonPath("$.description", is("Updated description")));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/companies/types - Should fail with invalid name")
    void updateType_WithInvalidName_ShouldReturn400() throws Exception {
        CompanyTypeDTO updateRequest = new CompanyTypeDTO(
                testType.getId(),
                "AB", // Invalid: less than 3 characters
                "Valid description"
        );

        mockMvc.perform(put("/api/companies/types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/companies/types/{id} - Should delete company type without companies")
    void deleteType_WithoutCompanies_ShouldSucceed() throws Exception {
        // Create a new type without any companies
        CompanyType typeToDelete = new CompanyType();
        typeToDelete.setName("Type To Delete " + System.currentTimeMillis());
        typeToDelete.setCreateDate(LocalDate.now());
        typeToDelete.setDescription("This type will be deleted");
        typeToDelete = companyTypeRepo.save(typeToDelete);

        mockMvc.perform(delete("/api/companies/types/{id}", typeToDelete.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(typeToDelete.getId().toString()));

        // Verify deletion
        Assertions.assertTrue(companyTypeRepo.findById(typeToDelete.getId()).isEmpty());
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/companies/types/{id} - Should fail when type has associated companies")
    void deleteType_WithCompanies_ShouldFail() throws Exception {
        // testType already has testCompany associated with it
        mockMvc.perform(delete("/api/companies/types/{id}", testType.getId()))
                .andExpect(status().isBadRequest());
    }

    //  Funding Request Endpoint 

    @Test
    @WithMockUser(authorities = "PERMISSION_Submit funding")
    @DisplayName("POST /api/companies/funding - Should submit funding request with proper authority")
    void submitFundingRequest_WithAuthority_ShouldSucceed() throws Exception {
        // Create a campaign for the funding request
        Campaign campaign = new Campaign();
        campaign.setName("Test Campaign " + System.currentTimeMillis());
        campaign.setCompany(testCompany);
        campaign.setStatus(CampaignStatus.ACTIVE);
        campaign.setCreateDate(LocalDate.now());
        campaign.setDescription("Test campaign description");
        campaign.setEndDate(LocalDate.now().plusWeeks(4));
        campaign.setStartDate(LocalDate.now());
        campaign.setRemainingFunds(BigDecimal.ZERO);
        campaign.setAvailableFunds(new BigDecimal("5000"));
        campaign.setRaisedFunds(new BigDecimal("5000"));
        campaign.setFundGoal(new BigDecimal("10000"));
        Campaign savedCampaign = campaignRepo.save(campaign);

        FundRequestCreateReq request = new FundRequestCreateReq(
                "This is a detailed message explaining why funds are needed for this campaign. It must be at least 30 characters.",
                new BigDecimal("1000.00"),
                savedCampaign.getId(),
                testCompany.getId()
        );

        mockMvc.perform(post("/api/companies/funding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount", is(1000.0)))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @WithMockUser // No specific authority
    @DisplayName("POST /api/companies/funding - Should return 403 without proper authority")
    void submitFundingRequest_WithoutAuthority_ShouldReturn403() throws Exception {
        FundRequestCreateReq request = new FundRequestCreateReq(
                "This is a detailed message explaining why funds are needed for this campaign. It must be at least 30 characters.",
                new BigDecimal("1000.00"),
                1,
                testCompany.getId()
        );

        mockMvc.perform(post("/api/companies/funding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    //  Authentication Tests 

    @Test
    @DisplayName("GET /api/companies - Should return 401 without authentication")
    void getAll_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/companies - Should return 401 without authentication")
    void create_WithoutAuth_ShouldReturn401() throws Exception {
        CompanyCreateReq request = new CompanyCreateReq(
                1,
                "Test Company",
                "REG123456",
                "TAX12345678",
                1
        );

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
