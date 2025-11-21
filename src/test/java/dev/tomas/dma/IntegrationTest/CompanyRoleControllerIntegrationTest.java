package dev.tomas.dma.IntegrationTest;

import dev.tomas.dma.dto.request.CompanyRoleCreateReq;
import dev.tomas.dma.dto.request.CompanyRoleUpdateReq;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.entity.CompanyPermission;
import dev.tomas.dma.entity.CompanyRole;
import dev.tomas.dma.entity.CompanyType;
import dev.tomas.dma.enums.PermissionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

class CompanyRoleControllerIntegrationTest extends BaseIntegrationTest {

    private Company testCompany;
    private CompanyPermission testPermission1;
    private CompanyPermission testPermission2;

    @BeforeEach
    void setUpTest() {
        // Create test company type
        CompanyType type = new CompanyType();
        type.setName("Tech");
        type.setDescription("Tech company");
        type = companyTypeRepo.save(type);

        // Create test company
        testCompany = new Company();
        testCompany.setName("Test Company");
        testCompany.setRegistrationNumber("REG123");
        testCompany.setTaxId("TAX123");
        testCompany.setType(type);
        testCompany = companyRepo.save(testCompany);

        // Create test permissions
        testPermission1 = new CompanyPermission();
        testPermission1.setName("List roles");
        testPermission1.setType(PermissionType.COMPANY_ROLE_MANAGEMENT);
        testPermission1.setDescription("Permission to list roles");
        testPermission1 = companyPermissionRepo.save(testPermission1);

        testPermission2 = new CompanyPermission();
        testPermission2.setName("Create role");
        testPermission2.setType(PermissionType.COMPANY_ROLE_MANAGEMENT);
        testPermission2.setDescription("Permission to create roles");
        testPermission2 = companyPermissionRepo.save(testPermission2);
    }

    // ==================== Get All Roles Tests ====================

    @Test
    @WithMockUser(authorities = {"PERMISSION_List roles"})
    void shouldGetAllRolesByCompanyId() throws Exception {
        // Given
        createTestRole("Manager", testCompany, List.of(testPermission1));
        createTestRole("Employee", testCompany, List.of(testPermission2));

        // When & Then
        mockMvc.perform(get("/api/companies/roles/{companyId}", testCompany.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasSize(2)))
                .andExpect(jsonPath("$.roles[0].name").exists())
                .andExpect(jsonPath("$.roles[0].companyId").value(testCompany.getId()))
                .andExpect(jsonPath("$.roles[0].permissions").exists());
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION_List roles"})
    void shouldReturnEmptyListWhenCompanyHasNoRoles() throws Exception {
        mockMvc.perform(get("/api/companies/roles/{companyId}", testCompany.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasSize(0)));
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION_List roles"})
    void shouldReturnEmptyListForNonExistentCompany() throws Exception {
        mockMvc.perform(get("/api/companies/roles/{companyId}", 99999))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles", hasSize(0)));
    }

    @Test
    @WithMockUser // No permission
    void shouldReturn401WhenGettingRolesWithoutPermission() throws Exception {
        mockMvc.perform(get("/api/companies/roles/{companyId}", testCompany.getId()))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Create Role Tests ====================

    @Test
    @WithMockUser(authorities = {"PERMISSION_Create role"})
    void shouldCreateRoleSuccessfully() throws Exception {
        // Given
        CompanyRoleCreateReq request = new CompanyRoleCreateReq();
        request.setName("Senior Developer");
        request.setCompanyId(testCompany.getId());
        request.setPermissionIds(List.of(testPermission1.getId(), testPermission2.getId()));

        // When & Then
        mockMvc.perform(post("/api/companies/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Senior Developer"))
                .andExpect(jsonPath("$.permissions", hasSize(2)));

        // Verify in database
        List<CompanyRole> roles = companyRoleRepo.findAllByCompanyId(testCompany.getId());
        assertEquals(1, roles.size());
        assertEquals("Senior Developer", roles.get(0).getName());
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION_Create role"})
    void shouldCreateRoleWithoutPermissions() throws Exception {
        // Given
        CompanyRoleCreateReq request = new CompanyRoleCreateReq();
        request.setName("Intern");
        request.setCompanyId(testCompany.getId());
        request.setPermissionIds(List.of()); // Empty permissions

        // When & Then
        mockMvc.perform(post("/api/companies/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Intern"))
                .andExpect(jsonPath("$.permissions", hasSize(0)));
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION_Create role"})
    void shouldReturn400WhenCreatingRoleWithBlankName() throws Exception {
        // Given: Blank name
        CompanyRoleCreateReq request = new CompanyRoleCreateReq();
        request.setName(""); // Blank
        request.setCompanyId(testCompany.getId());
        request.setPermissionIds(List.of(testPermission1.getId()));

        // When & Then
        mockMvc.perform(post("/api/companies/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION_Create role"})
    void shouldReturn400WhenCreatingRoleWithNameTooLong() throws Exception {
        // Given: Name too long (>100 characters)
        CompanyRoleCreateReq request = new CompanyRoleCreateReq();
        request.setName("A".repeat(101)); // Too long
        request.setCompanyId(testCompany.getId());
        request.setPermissionIds(List.of(testPermission1.getId()));

        // When & Then
        mockMvc.perform(post("/api/companies/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION_Create role"})
    void shouldReturn400WhenCreatingRoleWithInvalidCompanyId() throws Exception {
        // Given: Invalid company ID
        CompanyRoleCreateReq request = new CompanyRoleCreateReq();
        request.setName("Manager");
        request.setCompanyId(-1); // Invalid
        request.setPermissionIds(List.of(testPermission1.getId()));

        // When & Then
        mockMvc.perform(post("/api/companies/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION_Create role"})
    void shouldReturn404WhenCreatingRoleForNonExistentCompany() throws Exception {
        // Given
        CompanyRoleCreateReq request = new CompanyRoleCreateReq();
        request.setName("Manager");
        request.setCompanyId(99999);
        request.setPermissionIds(List.of(testPermission1.getId()));

        // When & Then
        mockMvc.perform(post("/api/companies/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION_Create role"})
    void shouldCreateRoleWithNonExistentPermission() throws Exception {
        // Given - Service doesn't validate permissions, just skips invalid ones
        CompanyRoleCreateReq request = new CompanyRoleCreateReq();
        request.setName("Manager");
        request.setCompanyId(testCompany.getId());
        request.setPermissionIds(List.of(99999));

        // When & Then
        mockMvc.perform(post("/api/companies/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Manager"))
                .andExpect(jsonPath("$.permissions", hasSize(0)));
    }

    @Test
    @WithMockUser // No permissions
    void shouldReturn401WhenCreatingRoleWithoutPermission() throws Exception {
        // Given
        CompanyRoleCreateReq request = new CompanyRoleCreateReq();
        request.setName("Manager");
        request.setCompanyId(testCompany.getId());
        request.setPermissionIds(List.of(testPermission1.getId()));

        // When & Then
        mockMvc.perform(post("/api/companies/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Update Role Tests ====================

    @Test
    @WithMockUser(authorities = {"PERMISSION_Modify role"})
    void shouldUpdateRoleSuccessfully() throws Exception {
        // Given
        CompanyRole role = createTestRole("Manager", testCompany, List.of(testPermission1));

        CompanyRoleUpdateReq request = new CompanyRoleUpdateReq();
        request.setId(role.getId());
        request.setName("Senior Manager");
        request.setPermissionIds(List.of(testPermission1.getId(), testPermission2.getId()));

        // When & Then
        mockMvc.perform(put("/api/companies/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Senior Manager"))
                .andExpect(jsonPath("$.permissions", hasSize(2)));

        // Verify in database
        CompanyRole updated = companyRoleRepo.findById(role.getId()).orElseThrow();
        assertEquals("Senior Manager", updated.getName());
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION_Modify role"})
    void shouldReturn404WhenUpdatingNonExistentRole() throws Exception {
        // Given
        CompanyRoleUpdateReq request = new CompanyRoleUpdateReq();
        request.setId(99999);
        request.setName("Manager");
        request.setPermissionIds(List.of(testPermission1.getId()));

        // When & Then
        mockMvc.perform(put("/api/companies/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser // No permissions
    void shouldReturn401WhenUpdatingRoleWithoutPermission() throws Exception {
        // Given
        CompanyRole role = createTestRole("Manager", testCompany, List.of(testPermission1));

        CompanyRoleUpdateReq request = new CompanyRoleUpdateReq();
        request.setId(role.getId());
        request.setName("Updated Name");
        request.setPermissionIds(List.of(testPermission1.getId()));

        // When & Then
        mockMvc.perform(put("/api/companies/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Delete Role Tests ====================

    @Test
    @WithMockUser(authorities = {"PERMISSION_Delete role"})
    void shouldDeleteRoleSuccessfully() throws Exception {
        // Given
        CompanyRole role = createTestRole("Manager", testCompany, List.of(testPermission1));

        // When & Then
        mockMvc.perform(delete("/api/companies/roles/{id}", role.getId()))
                .andExpect(status().isOk());

        // Verify deletion
        assertFalse(companyRoleRepo.existsById(role.getId()));
    }

    @Test
    @WithMockUser(authorities = {"PERMISSION_Delete role"})
    void shouldReturn404WhenDeletingNonExistentRole() throws Exception {
        mockMvc.perform(delete("/api/companies/roles/{id}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser // No permissions
    void shouldReturn401WhenDeletingRoleWithoutPermission() throws Exception {
        // Given
        CompanyRole role = createTestRole("Manager", testCompany, List.of(testPermission1));

        // When & Then
        mockMvc.perform(delete("/api/companies/roles/{id}", role.getId()))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Get Permissions Tests ====================

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void shouldGetAllPermissions() throws Exception {
        mockMvc.perform(get("/api/companies/roles/permissions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permissions").exists());
    }

    @Test
    @WithMockUser // No ROLE_ADMIN
    void shouldReturn401WhenGettingPermissionsWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/api/companies/roles/permissions"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Get Permission Types Tests ====================

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void shouldGetAllPermissionTypes() throws Exception {
        mockMvc.perform(get("/api/companies/roles/permissions/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.types").exists());
    }

    @Test
    @WithMockUser // No ROLE_ADMIN
    void shouldReturn401WhenGettingPermissionTypesWithoutAdminRole() throws Exception {
        mockMvc.perform(get("/api/companies/roles/permissions/types"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Helper Methods ====================

    private CompanyRole createTestRole(String name, Company company, List<CompanyPermission> permissions) {
        CompanyRole role = new CompanyRole();
        role.setName(name);
        role.setCompany(company);
        role.setPermissions(permissions);
        return companyRoleRepo.save(role);
    }
}