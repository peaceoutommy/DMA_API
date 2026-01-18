package dev.tomas.dma.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.PaymentIntent;
import dev.tomas.dma.dto.common.DonationDTO;
import dev.tomas.dma.entity.*;
import dev.tomas.dma.enums.CampaignStatus;
import dev.tomas.dma.enums.CompanyStatus;
import dev.tomas.dma.enums.UserRole;
import dev.tomas.dma.repository.*;
import dev.tomas.dma.service.ExternalStorageService;
import dev.tomas.dma.service.PaymentService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DonationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DonationRepository donationRepository;

    @Autowired
    private CampaignRepo campaignRepo;

    @Autowired
    private CompanyRepo companyRepo;

    @Autowired
    private CompanyTypeRepo companyTypeRepo;

    @Autowired
    private UserRepo userRepo;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private ExternalStorageService storageService;

    @MockitoBean
    private TicketService ticketService;

    private User testUser;
    private Campaign testCampaign;
    private Company testCompany;
    private CompanyType testType;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up in reverse order of dependencies
        donationRepository.deleteAll();
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

        // Delete test users
        userRepo.findAll().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().contains("donationtest"))
                .forEach(userRepo::delete);

        // Create test CompanyType
        testType = new CompanyType();
        testType.setName("Donation Test Type " + System.currentTimeMillis());
        testType.setCreateDate(LocalDate.now());
        testType.setDescription("Test type for donation tests");
        testType = companyTypeRepo.save(testType);

        // Create test Company
        testCompany = new Company();
        testCompany.setName("Donation Test Company " + System.currentTimeMillis());
        testCompany.setStatus(CompanyStatus.APPROVED);
        testCompany.setRegistrationNumber("DONREG" + System.currentTimeMillis());
        testCompany.setCreateDate(LocalDate.now());
        testCompany.setTaxId("DONTAX" + System.currentTimeMillis());
        testCompany.setType(testType);
        testCompany = companyRepo.save(testCompany);

        // Create test User
        testUser = new User();
        testUser.setEmail("donationtest" + System.currentTimeMillis() + "@example.com");
        testUser.setUsername("donationtestuser" + System.currentTimeMillis());
        testUser.setPassword("password123");
        testUser.setPhoneNumber("+1" + System.currentTimeMillis() % 10000000000L);
        testUser.setAddress("123 Donation Test Street");
        testUser.setFirstName("Donation");
        testUser.setLastName("Tester");
        testUser.setMiddleNames("Test");
        testUser.setRole(UserRole.DONOR);
        testUser.setEnabled(true);
        testUser = userRepo.save(testUser);

        // Create test Campaign
        testCampaign = new Campaign();
        testCampaign.setName("Donation Test Campaign " + System.currentTimeMillis());
        testCampaign.setCompany(testCompany);
        testCampaign.setStatus(CampaignStatus.ACTIVE);
        testCampaign.setCreateDate(LocalDate.now());
        testCampaign.setDescription("Test campaign for donation tests");
        testCampaign.setEndDate(LocalDate.now().plusWeeks(4));
        testCampaign.setStartDate(LocalDate.now());
        testCampaign.setRemainingFunds(BigDecimal.ZERO);
        testCampaign.setAvailableFunds(BigDecimal.ZERO);
        testCampaign.setRaisedFunds(BigDecimal.ZERO);
        testCampaign.setFundGoal(new BigDecimal("10000"));
        testCampaign = campaignRepo.save(testCampaign);

        // Mock PaymentService
        PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
        when(mockPaymentIntent.getClientSecret()).thenReturn("test_client_secret_12345");
        when(paymentService.createPaymentIntent(any())).thenReturn(mockPaymentIntent);
    }

    @AfterEach
    void tearDown() {
        donationRepository.deleteAll();
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

        userRepo.findAll().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().contains("donationtest"))
                .forEach(userRepo::delete);
    }

    // ==================== Create Donation (Payment Intent) Tests ====================

    @Test
    @WithMockUser
    @DisplayName("POST /api/donations - Should return client secret for payment")
    void donate_ShouldReturnClientSecret() throws Exception {
        DonationDTO request = new DonationDTO();
        request.setCampaignId(testCampaign.getId());
        request.setUserId(testUser.getId());
        request.setAmount(5000L); // $50.00 in cents

        mockMvc.perform(post("/api/donations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientSecret", is("test_client_secret_12345")));
    }

    @Test
    @DisplayName("POST /api/donations - Should return 401 without authentication")
    void donate_WithoutAuth_ShouldReturn401() throws Exception {
        DonationDTO request = new DonationDTO();
        request.setCampaignId(testCampaign.getId());
        request.setUserId(testUser.getId());
        request.setAmount(5000L);

        mockMvc.perform(post("/api/donations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Get Donations By User Tests ====================

    @Test
    @WithMockUser
    @DisplayName("GET /api/donations/user/{userId} - Should return donations for user")
    void getAllByUserId_ShouldReturnUserDonations() throws Exception {
        // Create donations for test user
        createDonationInDb(testUser, testCampaign, 5000L);
        createDonationInDb(testUser, testCampaign, 10000L);

        mockMvc.perform(get("/api/donations/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId", is(testUser.getId())))
                .andExpect(jsonPath("$[0].campaignId", is(testCampaign.getId())));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/donations/user/{userId} - Should return empty for user with no donations")
    void getAllByUserId_NoDonations_ShouldReturnEmpty() throws Exception {
        mockMvc.perform(get("/api/donations/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/donations/user/{userId} - Should return donations with campaign and company details")
    void getAllByUserId_ShouldIncludeCampaignDetails() throws Exception {
        createDonationInDb(testUser, testCampaign, 7500L);

        mockMvc.perform(get("/api/donations/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].campaignName", is(testCampaign.getName())))
                .andExpect(jsonPath("$[0].companyName", is(testCompany.getName())))
                .andExpect(jsonPath("$[0].companyId", is(testCompany.getId())))
                .andExpect(jsonPath("$[0].amount", is(7500)));
    }

    @Test
    @DisplayName("GET /api/donations/user/{userId} - Should return 401 without authentication")
    void getAllByUserId_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/donations/user/{userId}", testUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Multiple Users Donations Tests ====================

    @Test
    @WithMockUser
    @DisplayName("GET /api/donations/user/{userId} - Should only return donations for specified user")
    void getAllByUserId_MultipleUsers_ShouldReturnOnlySpecifiedUser() throws Exception {
        // Create another user
        User anotherUser = new User();
        anotherUser.setEmail("donationtest.another" + System.currentTimeMillis() + "@example.com");
        anotherUser.setUsername("anotherdonor" + System.currentTimeMillis());
        anotherUser.setPassword("password123");
        anotherUser.setPhoneNumber("+2" + System.currentTimeMillis() % 10000000000L);
        anotherUser.setAddress("456 Another Street");
        anotherUser.setFirstName("Another");
        anotherUser.setLastName("Donor");
        anotherUser.setMiddleNames("Middle");
        anotherUser.setRole(UserRole.DONOR);
        anotherUser.setEnabled(true);
        anotherUser = userRepo.save(anotherUser);

        // Create donations for both users
        createDonationInDb(testUser, testCampaign, 5000L);
        createDonationInDb(testUser, testCampaign, 3000L);
        createDonationInDb(anotherUser, testCampaign, 10000L);

        // Should only return testUser's donations
        mockMvc.perform(get("/api/donations/user/{userId}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].userId", everyItem(is(testUser.getId()))));

        // Should only return anotherUser's donations
        mockMvc.perform(get("/api/donations/user/{userId}", anotherUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].userId", is(anotherUser.getId())));
    }

    // ==================== Webhook Tests ====================

    @Test
    @DisplayName("POST /api/donations/webhook - Should return 400 for invalid signature")
    void handleWebhook_InvalidSignature_ShouldReturn400() throws Exception {
        String payload = "{\"type\": \"payment_intent.succeeded\"}";

        mockMvc.perform(post("/api/donations/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Stripe-Signature", "invalid_signature"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid signature"));
    }

    // ==================== Helper Methods ====================

    private Donation createDonationInDb(User user, Campaign campaign, Long amount) {
        Donation donation = new Donation();
        donation.setUser(user);
        donation.setCampaign(campaign);
        donation.setAmount(amount);
        donation.setDate(LocalDateTime.now());
        return donationRepository.save(donation);
    }
}
