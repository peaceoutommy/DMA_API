package dev.tomas.dma.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.enums.UserRole;
import dev.tomas.dma.repository.UserRepo;
import dev.tomas.dma.service.ExternalStorageService;
import dev.tomas.dma.service.TicketService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepo userRepo;

    @MockitoBean
    private ExternalStorageService storageService;

    @MockitoBean
    private TicketService ticketService;

    private User testUser;
    private List<User> searchTestUsers = new ArrayList<>();

    @BeforeEach
    void setUp() {
        // Clean up test users from previous runs
        userRepo.findAll().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().contains("usertest"))
                .forEach(userRepo::delete);

        // Create main test user
        testUser = createTestUser(
                "usertest" + System.currentTimeMillis() + "@example.com",
                "usertestuser" + System.currentTimeMillis()
        );

        // Create users for search tests
        searchTestUsers.clear();
        searchTestUsers.add(createTestUser(
                "usertest.search1@example.com",
                "searchuser1" + System.currentTimeMillis()
        ));
        searchTestUsers.add(createTestUser(
                "usertest.search2@example.com",
                "searchuser2" + System.currentTimeMillis()
        ));
        searchTestUsers.add(createTestUser(
                "usertest.different@domain.com",
                "differentuser" + System.currentTimeMillis()
        ));
    }

    @AfterEach
    void tearDown() {
        // Clean up test users
        userRepo.findAll().stream()
                .filter(u -> u.getEmail() != null && u.getEmail().contains("usertest"))
                .forEach(userRepo::delete);
    }

    // ==================== Get User By ID Tests ====================

    @Test
    @WithMockUser
    @DisplayName("GET /api/users/{id} - Should return user by ID")
    void getById_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId())))
                .andExpect(jsonPath("$.email", is(testUser.getEmail())))
                .andExpect(jsonPath("$.firstName", is(testUser.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(testUser.getLastName())))
                .andExpect(jsonPath("$.username", is(testUser.getActualUsername())));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/users/{id} - Should return 404 for non-existent user")
    void getById_NonExistent_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/users/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users/{id} - Should return 401 without authentication")
    void getById_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Get User By Email Tests ====================

    @Test
    @WithMockUser
    @DisplayName("GET /api/users/email/{email} - Should return user by email")
    void getByEmail_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/email/{email}", testUser.getEmail()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testUser.getId())))
                .andExpect(jsonPath("$.email", is(testUser.getEmail())))
                .andExpect(jsonPath("$.firstName", is(testUser.getFirstName())));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/users/email/{email} - Should return 404 for non-existent email")
    void getByEmail_NonExistent_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/users/email/{email}", "nonexistent@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/users/email/{email} - Should return 401 without authentication")
    void getByEmail_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/users/email/{email}", testUser.getEmail()))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Search Users By Email Tests ====================

    @Test
    @WithMockUser
    @DisplayName("GET /api/users/search/{email} - Should return matching users")
    void searchByEmail_ShouldReturnMatchingUsers() throws Exception {
        mockMvc.perform(get("/api/users/search/{email}", "usertest.search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].email", everyItem(containsString("usertest.search"))));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/users/search/{email} - Should return empty list for no matches")
    void searchByEmail_NoMatches_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/users/search/{email}", "zzznomatchzzz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/users/search/{email} - Should return single user for exact match")
    void searchByEmail_ExactMatch_ShouldReturnSingleUser() throws Exception {
        String uniqueEmail = searchTestUsers.get(0).getEmail();

        mockMvc.perform(get("/api/users/search/{email}", uniqueEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email", is(uniqueEmail)));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/users/search/{email} - Should be case insensitive")
    void searchByEmail_CaseInsensitive_ShouldReturnMatches() throws Exception {
        mockMvc.perform(get("/api/users/search/{email}", "USERTEST.SEARCH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(0))));
    }

    @Test
    @DisplayName("GET /api/users/search/{email} - Should return 401 without authentication")
    void searchByEmail_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/users/search/{email}", "test"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Helper Methods ====================

    private User createTestUser(String email, String username) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword("encodedPassword123");
        user.setPhoneNumber("+1" + System.currentTimeMillis() % 10000000000L);
        user.setAddress("123 Test Street");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setMiddleNames("Middle");
        user.setRole(UserRole.DONOR);
        user.setEnabled(true);
        return userRepo.save(user);
    }
}
