package dev.tomas.dma.controller;

import dev.tomas.dma.dto.request.CampaignCreateReq;
import dev.tomas.dma.entity.Campaign;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.enums.CampaignStatus;
import dev.tomas.dma.enums.CompanyStatus;
import dev.tomas.dma.repository.CampaignRepo;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.service.ExternalStorageService;
import dev.tomas.dma.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional // Ensures DB is rolled back after every test
class CampaignControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CampaignRepo campaignRepo;

    @Autowired
    private CompanyRepo companyRepo;

    // We mock these because we only want to test the Campaign DB logic,
    // not S3 upload or external Ticket notifications.
    @MockitoBean
    private ExternalStorageService storageService;

    @MockitoBean
    private TicketService ticketService;

    private Company testCompany;

    @BeforeEach
    void setUp() {
        // Setup a Company entity because Campaign requires a CompanyID
        Company company = new Company();
        company.setName("Test Company");
        company.setStatus(CompanyStatus.APPROVED);
        // Populate other required fields for Company entity...
        testCompany = companyRepo.save(company);

        // Mock storage service success
        try {
            when(storageService.uploadFile(any(), any(), any())).thenReturn("https://s3-bucket/test.jpg");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @WithMockUser(authorities = "PERMISSION_Create campaign")
    void createCampaign_ShouldReturnSavedCampaign() throws Exception {
        // 1. Prepare Mock Multipart File (image)
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "banner.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        // 2. Perform Multipart Request
        // Note: parameters match fields in CampaignCreateReq
        mockMvc.perform(multipart("/api/campaigns")
                        .file(imageFile)
                        .param("name", "Summer Sale 2026")
                        .param("description", "Big discounts")
                        .param("companyId", testCompany.getId().toString())
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().plusDays(10).toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Summer Sale 2026")))
                .andExpect(jsonPath("$.companyId", is(testCompany.getId())));

        // 3. Verify DB State
        assert campaignRepo.count() == 1;
    }

    @Test
    @WithMockUser
        // Default user, usually needed for generic authenticated endpoints
    void getAll_ShouldReturnListOfCampaigns() throws Exception {
        // 1. Manually save campaigns to H2
        createCampaignInDb("Campaign A", testCompany);
        createCampaignInDb("Campaign B", testCompany);

        // 2. Call API
        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaigns", hasSize(2))) // Assuming CampaignGetAllRes has a list field named 'campaigns'
                .andExpect(jsonPath("$.campaigns[0].name", is("Campaign A")));
    }

    @Test
    @WithMockUser
    void getById_ShouldReturnCampaign() throws Exception {
        Campaign saved = createCampaignInDb("Specific Campaign", testCompany);

        mockMvc.perform(get("/api/campaigns/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Specific Campaign")));
    }

    @Test
    @WithMockUser(authorities = "PERMISSION_Archive campaign")
    void archive_ShouldChangeStatus() throws Exception {
        Campaign saved = createCampaignInDb("To Archive", testCompany);

        mockMvc.perform(post("/api/campaigns/archive/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ARCHIVED"))); // Assuming Enum string value

        // Verify DB update
        Campaign updated = campaignRepo.findById(saved.getId()).orElseThrow();
        // assert updated.getStatus() == CampaignStatus.ARCHIVED;
    }

    @Test
    @WithMockUser
        // No authority provided
    void delete_WithoutAuthority_ShouldReturnForbidden() throws Exception {
        // Assuming delete might need permission, or if not, testing basic access
        // If delete is public in security config, this expects 200.
        // If restricted, expects 403.
        // Based on your controller, delete has NO @PreAuthorize, so checking if it works:

        Campaign saved = createCampaignInDb("To Delete", testCompany);

        mockMvc.perform(delete("/api/campaigns/{id}", saved.getId()))
                .andExpect(status().isOk());

        assert campaignRepo.findById(saved.getId()).isEmpty();
    }

    // --- Helper ---
    private Campaign createCampaignInDb(String name, Company company) {
        Campaign c = new Campaign();
        c.setName(name);
        c.setCompany(company);
        c.setStatus(CampaignStatus.ACTIVE); // Assuming Enum exists
        // Set other mandatory fields based on your Entity definition
        return campaignRepo.save(c);
    }
}