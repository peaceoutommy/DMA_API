package dev.tomas.dma.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tomas.dma.config.StripeConfig;
import dev.tomas.dma.dto.request.CampaignUpdateReq;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.enums.CampaignStatus;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.service.ExternalStorageService;
import dev.tomas.dma.service.FileService;
import dev.tomas.dma.service.JWTService;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ===========================
 * FULL INTEGRATION TEST
 * ===========================
 *
 * This test loads:
 * ✅ Controller
 * ✅ Service
 * ✅ Repository
 * ✅ Mapper
 * ✅ H2 Database
 *
 * BUT it mocks:
 * ❌ ExternalStorageService (no real file upload)
 * ❌ FileService (no real file write)
 *
 * This gives us REAL business logic without dangerous side effects.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // ✅ Disables Spring Security (@PreAuthorize)
@ActiveProfiles("test")                  // ✅ Uses application-test.yml
@Transactional                           // ✅ Rollback everything after each test
class CampaignControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // ✅ Used to simulate real HTTP requests

    @Autowired
    private ObjectMapper objectMapper; // ✅ Converts objects ↔ JSON

    @Autowired
    private CompanyRepo companyRepo; // ✅ We must insert a Company first (FK requirement)

    // ✅ These are real dependencies in your service,
    // but we MOCK them to avoid real file uploads
    @MockitoBean
    private ExternalStorageService externalStorageService;
    @MockitoBean
    private FileService fileService;
    @MockitoBean
    private JWTService  jwtService;
    @MockitoBean
    private StripeConfig  stripeConfig;
    private Integer companyId;

    /**
     * ===========================
     * SETUP BEFORE EACH TEST
     * ===========================
     */
    @BeforeEach
    void setup() {
        Company company = new Company();
        company.setName("Test Company");
        company.setRegistrationNumber("REG-123");
        company.setTaxId("TAX-123");

        companyId = companyRepo.save(company).getId();
    }

    /**
     * ===========================
     * ✅ TEST: CREATE CAMPAIGN
     * ===========================
     *
     * This test verifies:
     * HTTP → Controller → Service → Repository → H2 Database
     */
    @Test
    @WithMockUser(authorities = "PERMISSION_Create campaign")
    void shouldCreateCampaign() throws Exception {

        // ✅ Fake image file upload (multipart/form-data)
        MockMultipartFile image = new MockMultipartFile(
                "images",
                "test.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-image-content".getBytes()
        );

        mockMvc.perform(
                        multipart("/api/campaigns")
                                .file(image)
                                .param("name", "Save the Forest")
                                .param("description", "This campaign is created for forest protection")
                                .param("companyId", companyId.toString())
                                .param("fundGoal", "10000")
                                .param("startDate", LocalDate.now().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Save the Forest"))
                .andExpect(jsonPath("$.fundGoal").value(10000))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    /**
     * ===========================
     * ✅ TEST: GET ALL CAMPAIGNS
     * ===========================
     */
    @Test
    void shouldGetAllCampaigns() throws Exception {
        mockMvc.perform(get("/api/campaigns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.campaigns").isArray());
    }

    /**
     * ===========================
     * ✅ TEST: UPDATE CAMPAIGN
     * ===========================
     */
    @Test
    @WithMockUser(authorities = "PERMISSION_Update campaign")
    void shouldUpdateCampaign() throws Exception {

        // 🔹 First create a campaign
        shouldCreateCampaign();

        CampaignUpdateReq updateReq = new CampaignUpdateReq();
        updateReq.setId(1);
        updateReq.setName("Updated Campaign");
        updateReq.setDescription("Updated description with enough characters");
        updateReq.setCompanyId(companyId);
        updateReq.setFundGoal(BigDecimal.valueOf(20000));
        updateReq.setStatus(CampaignStatus.ACTIVE);

        mockMvc.perform(put("/api/campaigns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Campaign"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    /**
     * ===========================
     * ✅ TEST: ARCHIVE CAMPAIGN
     * ===========================
     */
    @Test
    @WithMockUser(authorities = "PERMISSION_Archive campaign")
    void shouldArchiveCampaign() throws Exception {

        shouldCreateCampaign();

        mockMvc.perform(post("/api/campaigns/archive/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    /**
     * ===========================
     * ✅ TEST: DELETE CAMPAIGN
     * ===========================
     */
    @Test
    void shouldDeleteCampaign() throws Exception {

        shouldCreateCampaign();

        mockMvc.perform(delete("/api/campaigns/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }
}
