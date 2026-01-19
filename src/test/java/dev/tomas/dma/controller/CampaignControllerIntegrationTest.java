package dev.tomas.dma.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tomas.dma.dto.request.CampaignUpdateReq;
import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.entity.CompanyType;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.enums.CampaignStatus;
import dev.tomas.dma.enums.CompanyStatus;
import dev.tomas.dma.enums.UserRole;
import dev.tomas.dma.repository.CampaignRepo;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.repository.CompanyTypeRepo;
import dev.tomas.dma.repository.UserRepo;
import dev.tomas.dma.service.ExternalStorageService;
import dev.tomas.dma.service.TicketService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CampaignControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CampaignRepo campaignRepo;

    @Autowired
    private CompanyRepo companyRepo;

    @Autowired
    private CompanyTypeRepo companyTypeRepo;

    @Autowired
    private UserRepo userRepo;

    @MockitoBean
    private ExternalStorageService storageService;

    @MockitoBean
    private TicketService ticketService;

    private CompanyType testType;
    private Company testCompany;

    @BeforeEach
    void setUp() {
        // Clean up in reverse order of dependencies
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

        // Create test CompanyType
        CompanyType type = new CompanyType();
        type.setName("Campaign Test Type " + System.currentTimeMillis());
        type.setCreateDate(LocalDate.now());
        type.setDescription("Test type for campaign integration tests");
        testType = companyTypeRepo.save(type);

        // Create test Company
        Company company = new Company();
        company.setName("Campaign Test Company " + System.currentTimeMillis());
        company.setStatus(CompanyStatus.APPROVED);
        company.setRegistrationNumber("CAMPREG" + System.currentTimeMillis());
        company.setCreateDate(LocalDate.now());
        company.setTaxId("CAMPTAX" + System.currentTimeMillis());
        company.setType(testType);
        testCompany = companyRepo.save(company);

        // Mock storage service success
        try {
            when(storageService.uploadFile(any(), any(), any())).thenReturn("https://res.cloudinary.com/demo/image/upload/sample.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        campaignRepo.deleteAll();

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
    }

    //  Create Campaign Tests 

    @Test
    @WithMockUser(authorities = "PERMISSION_Create campaign")
    @DisplayName("POST /api/campaigns - Should create campaign with valid data")
    void createCampaign_ShouldReturnSavedCampaign() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "banner.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/campaigns")
                        .file(imageFile)
                        .param("name", "Summer Sale " + System.currentTimeMillis())
                        .param("description", "Big discounts for everyone")
                        .param("companyId", testCompany.getId().toString())
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().plusDays(30).toString())
                        .param("fundGoal", "10000.00")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", containsString("Summer Sale")))
                .andExpect(jsonPath("$.companyId", is(testCompany.getId())))
                .andExpect(jsonPath("$.status", is("PENDING")));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/campaigns - Should return 403 without proper authority")
    void createCampaign_WithoutAuthority_ShouldReturn403() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "banner.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/campaigns")
                        .file(imageFile)
                        .param("name", "Test Campaign")
                        .param("description", "Test description")
                        .param("companyId", testCompany.getId().toString())
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().plusDays(10).toString())
                        .param("fundGoal", "5000.00")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isForbidden());
    }

    //  Get All Campaigns Tests 

    @Test
    @WithMockUser
    @DisplayName("GET /api/campaigns - Should return approved campaigns for regular user")
    void getAll_AsRegularUser_ShouldReturnApprovedCampaigns() throws Exception {
        // Create approved and pending campaigns
        createCampaignInDb("Approved Campaign", testCompany, CampaignStatus.APPROVED);
        createCampaignInDb("Pending Campaign", testCompany, CampaignStatus.PENDING);

        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaigns", hasSize(1)))
                .andExpect(jsonPath("$.campaigns[0].status", is("APPROVED")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/campaigns - Should return all campaigns for admin")
    void getAll_AsAdmin_ShouldReturnAllCampaigns() throws Exception {
        createCampaignInDb("Approved Campaign", testCompany, CampaignStatus.APPROVED);
        createCampaignInDb("Pending Campaign", testCompany, CampaignStatus.PENDING);
        createCampaignInDb("Archived Campaign", testCompany, CampaignStatus.ARCHIVED);

        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaigns", hasSize(3)));
    }

    @Test
    @DisplayName("GET /api/campaigns - Should return 200 without authentication (public endpoint)")
    void getAll_WithoutAuth_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isOk());
    }

    //  Get Campaign By ID Tests 

    @Test
    @WithMockUser
    @DisplayName("GET /api/campaigns/{id} - Should return campaign by ID")
    void getById_ShouldReturnCampaign() throws Exception {
        Campaign saved = createCampaignInDb("Specific Campaign", testCompany, CampaignStatus.APPROVED);

        mockMvc.perform(get("/api/campaigns/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(saved.getId())))
                .andExpect(jsonPath("$.name", is("Specific Campaign")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/campaigns/{id} - Should return 404 for non-existent campaign")
    void getById_NonExistent_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/campaigns/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    //  Get Campaigns By Status Tests 

    @Test
    @WithMockUser
    @DisplayName("GET /api/campaigns/status/{status} - Should return campaigns by status")
    void getByStatus_ShouldReturnFilteredCampaigns() throws Exception {
        createCampaignInDb("Active Campaign 1", testCompany, CampaignStatus.ACTIVE);
        createCampaignInDb("Active Campaign 2", testCompany, CampaignStatus.ACTIVE);
        createCampaignInDb("Archived Campaign", testCompany, CampaignStatus.ARCHIVED);

        mockMvc.perform(get("/api/campaigns/status/{status}", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaigns", hasSize(2)))
                .andExpect(jsonPath("$.campaigns[*].status", everyItem(is("ACTIVE"))));
    }

    //  Get Campaigns By Company Tests 

    @Test
    @WithMockUser
    @DisplayName("GET /api/campaigns/company/{companyId} - Should return campaigns for company")
    void getByCompany_ShouldReturnCompanyCampaigns() throws Exception {
        createCampaignInDb("Company Campaign 1", testCompany, CampaignStatus.APPROVED);
        createCampaignInDb("Company Campaign 2", testCompany, CampaignStatus.ACTIVE);

        mockMvc.perform(get("/api/campaigns/company/{companyId}", testCompany.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaigns", hasSize(2)));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/campaigns/company/{companyId} - Should return empty for company with no campaigns")
    void getByCompany_NoCampaigns_ShouldReturnEmpty() throws Exception {
        // Create another company with no campaigns
        Company emptyCompany = new Company();
        emptyCompany.setName("Empty Company " + System.currentTimeMillis());
        emptyCompany.setStatus(CompanyStatus.APPROVED);
        emptyCompany.setRegistrationNumber("EMPTY" + System.currentTimeMillis());
        emptyCompany.setCreateDate(LocalDate.now());
        emptyCompany.setTaxId("EMPTYTAX" + System.currentTimeMillis());
        emptyCompany.setType(testType);
        emptyCompany = companyRepo.save(emptyCompany);

        mockMvc.perform(get("/api/campaigns/company/{companyId}", emptyCompany.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaigns", hasSize(0)));
    }

    //  Update Campaign Tests 

    @Test
    @WithMockUser(authorities = "PERMISSION_Update campaign")
    @DisplayName("PUT /api/campaigns - Should update campaign with valid data")
    void update_ShouldReturnUpdatedCampaign() throws Exception {
        Campaign saved = createCampaignInDb("Original Name", testCompany, CampaignStatus.APPROVED);

        CampaignUpdateReq updateReq = new CampaignUpdateReq();
        updateReq.setId(saved.getId());
        updateReq.setName("Updated Name");
        updateReq.setDescription("Updated description");

        mockMvc.perform(put("/api/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.description", is("Updated description")));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/campaigns - Should return 403 without proper authority")
    void update_WithoutAuthority_ShouldReturn403() throws Exception {
        Campaign saved = createCampaignInDb("Test Campaign", testCompany, CampaignStatus.APPROVED);

        CampaignUpdateReq updateReq = new CampaignUpdateReq();
        updateReq.setId(saved.getId());
        updateReq.setName("Updated Name");

        mockMvc.perform(put("/api/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());
    }

    //  Archive Campaign Tests 

    @Test
    @WithMockUser(authorities = "PERMISSION_Archive campaign")
    @DisplayName("POST /api/campaigns/archive/{id} - Should archive campaign")
    void archive_ShouldChangeStatusToArchived() throws Exception {
        Campaign saved = createCampaignInDb("To Archive", testCompany, CampaignStatus.ACTIVE);

        mockMvc.perform(post("/api/campaigns/archive/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ARCHIVED")));

        // Verify DB update
        Campaign updated = campaignRepo.findById(saved.getId()).orElseThrow();
        Assertions.assertEquals(CampaignStatus.ARCHIVED, updated.getStatus());
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/campaigns/archive/{id} - Should return 403 without proper authority")
    void archive_WithoutAuthority_ShouldReturn403() throws Exception {
        Campaign saved = createCampaignInDb("Test Campaign", testCompany, CampaignStatus.ACTIVE);

        mockMvc.perform(post("/api/campaigns/archive/{id}", saved.getId()))
                .andExpect(status().isForbidden());
    }

    //  Delete Campaign Tests 

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/campaigns/{id} - Should delete campaign")
    void delete_ShouldRemoveCampaign() throws Exception {
        Campaign saved = createCampaignInDb("To Delete", testCompany, CampaignStatus.PENDING);

        mockMvc.perform(delete("/api/campaigns/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string(saved.getId().toString()));

        Assertions.assertTrue(campaignRepo.findById(saved.getId()).isEmpty());
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/campaigns/{id} - Should return 404 for non-existent campaign")
    void delete_NonExistent_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/campaigns/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    //  Helper Methods 

    private Campaign createCampaignInDb(String name, Company company, CampaignStatus status) {
        Campaign c = new Campaign();
        c.setName(name);
        c.setCompany(company);
        c.setStatus(status);
        c.setCreateDate(LocalDate.now());
        c.setDescription("Test description for " + name);
        c.setEndDate(LocalDate.now().plusWeeks(4));
        c.setStartDate(LocalDate.now());
        c.setRemainingFunds(BigDecimal.ZERO);
        c.setAvailableFunds(BigDecimal.ZERO);
        c.setRaisedFunds(BigDecimal.ZERO);
        c.setFundGoal(new BigDecimal("10000"));
        return campaignRepo.save(c);
    }
}
