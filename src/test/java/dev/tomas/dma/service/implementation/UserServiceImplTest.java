package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.common.UserDTO;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.entity.CompanyRole;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.mapper.UserMapper;
import dev.tomas.dma.repository.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        Company company = new Company();
        company.setId(1);
        company.setName("Test Company");

        CompanyRole role = new CompanyRole();
        role.setId(1);
        role.setName("Employee");
        role.setCompany(company);

        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("user@example.com");
        testUser.setUsername("testuser");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setMiddleNames("Middle");
        testUser.setPhoneNumber("1234567890");
        testUser.setAddress("123 Test St");
        testUser.setCompany(company);
        testUser.setCompanyRole(role);

        testUserDTO = new UserDTO();
        testUserDTO.setId(1);
        testUserDTO.setEmail("user@example.com");
        testUserDTO.setUsername("testuser");
        testUserDTO.setFirstName("John");
        testUserDTO.setLastName("Doe");
        testUserDTO.setMiddleNames("Middle");
        testUserDTO.setPhoneNumber("1234567890");
        testUserDTO.setAddress("123 Test St");
        testUserDTO.setCompanyId(1);
        testUserDTO.setCompanyRoleId(1);
        testUserDTO.setCompanyRoleName("Employee");
    }

    @Nested
    @DisplayName("GetById Tests")
    class GetByIdTests {

        @Test
        @DisplayName("Should return user by id")
        void getById_Success() {
            when(userRepo.findById(1)).thenReturn(Optional.of(testUser));
            when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

            UserDTO result = userService.getById(1);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getEmail()).isEqualTo("user@example.com");
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when user not found")
        void getById_ThrowsException_WhenNotFound() {
            when(userRepo.findById(999)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getById(999))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User not found with id: 999");
        }
    }

    @Nested
    @DisplayName("GetByEmail Tests")
    class GetByEmailTests {

        @Test
        @DisplayName("Should return user by email")
        void getByEmail_Success() {
            when(userRepo.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
            when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

            UserDTO result = userService.getByEmail("user@example.com");

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when user not found by email")
        void getByEmail_ThrowsException_WhenNotFound() {
            when(userRepo.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getByEmail("notfound@example.com"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User not found with email: notfound@example.com");
        }
    }

    @Nested
    @DisplayName("SearchByEmail Tests")
    class SearchByEmailTests {

        @Test
        @DisplayName("Should return users matching email pattern")
        void searchByEmail_Success() {
            User secondUser = new User();
            secondUser.setId(2);
            secondUser.setEmail("user2@example.com");

            UserDTO secondUserDTO = new UserDTO();
            secondUserDTO.setId(2);
            secondUserDTO.setEmail("user2@example.com");

            when(userRepo.findByEmailContainingIgnoreCase("user")).thenReturn(Arrays.asList(testUser, secondUser));
            when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);
            when(userMapper.toDTO(secondUser)).thenReturn(secondUserDTO);

            List<UserDTO> result = userService.searchByEmail("user");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(UserDTO::getEmail)
                    .containsExactlyInAnyOrder("user@example.com", "user2@example.com");
        }

        @Test
        @DisplayName("Should return empty list when no users match")
        void searchByEmail_EmptyResult() {
            when(userRepo.findByEmailContainingIgnoreCase("nomatch")).thenReturn(new ArrayList<>());

            List<UserDTO> result = userService.searchByEmail("nomatch");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return single user when only one matches")
        void searchByEmail_SingleMatch() {
            when(userRepo.findByEmailContainingIgnoreCase("user@example")).thenReturn(Arrays.asList(testUser));
            when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

            List<UserDTO> result = userService.searchByEmail("user@example");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("Should perform case insensitive search")
        void searchByEmail_CaseInsensitive() {
            when(userRepo.findByEmailContainingIgnoreCase("USER")).thenReturn(Arrays.asList(testUser));
            when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

            List<UserDTO> result = userService.searchByEmail("USER");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should include company information in results")
        void searchByEmail_IncludesCompanyInfo() {
            when(userRepo.findByEmailContainingIgnoreCase("user")).thenReturn(Arrays.asList(testUser));
            when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

            List<UserDTO> result = userService.searchByEmail("user");

            assertThat(result.get(0).getCompanyId()).isEqualTo(1);
            assertThat(result.get(0).getCompanyRoleId()).isEqualTo(1);
            assertThat(result.get(0).getCompanyRoleName()).isEqualTo("Employee");
        }
    }
}
