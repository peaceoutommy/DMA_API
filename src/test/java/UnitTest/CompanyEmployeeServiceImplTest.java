package UnitTest;

import dev.tomas.dma.dto.common.UserDTO;
import dev.tomas.dma.dto.request.AddUserToCompanyReq;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.entity.CompanyRole;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.mapper.UserMapper;
import dev.tomas.dma.repository.CompanyEmployeeRepo;
import dev.tomas.dma.repository.CompanyRepo;
import dev.tomas.dma.repository.CompanyRoleRepo;
import dev.tomas.dma.repository.UserRepo;
import dev.tomas.dma.service.implementation.CompanyEmployeeServiceImpl;
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
class CompanyEmployeeServiceImplTest {

    @Mock
    private CompanyRoleRepo companyRoleRepo;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepo userRepo;

    @Mock
    private CompanyRepo companyRepo;

    @Mock
    private CompanyEmployeeRepo companyEmployeeRepo;

    @InjectMocks
    private CompanyEmployeeServiceImpl companyEmployeeService;

    private User testUser;
    private Company testCompany;
    private CompanyRole testRole;
    private UserDTO testUserDTO;
    private AddUserToCompanyReq addRequest;

    @BeforeEach
    void setUp() {
        testCompany = new Company();
        testCompany.setId(1);
        testCompany.setName("Test Company");

        testRole = new CompanyRole();
        testRole.setId(1);
        testRole.setName("Employee");
        testRole.setCompany(testCompany);

        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("employee@example.com");
        testUser.setUsername("employee");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

        testUserDTO = new UserDTO();
        testUserDTO.setId(1);
        testUserDTO.setEmail("employee@example.com");
        testUserDTO.setUsername("employee");
        testUserDTO.setFirstName("John");
        testUserDTO.setLastName("Doe");
        testUserDTO.setCompanyId(1);
        testUserDTO.setCompanyRoleId(1);
        testUserDTO.setCompanyRoleName("Employee");

        addRequest = new AddUserToCompanyReq();
        addRequest.setEmployeeId(1);
        addRequest.setCompanyId(1);
        addRequest.setRoleId(1);
    }

    @Nested
    @DisplayName("GetEmployeesByCompany Tests")
    class GetEmployeesByCompanyTests {

        @Test
        @DisplayName("Should return all employees for company")
        void getEmployeesByCompany_Success() {
            testUser.setCompany(testCompany);
            testUser.setCompanyRole(testRole);

            when(companyEmployeeRepo.findAllUsersByCompanyId(1)).thenReturn(Arrays.asList(testUser));
            when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);

            List<UserDTO> result = companyEmployeeService.getEmployeesByCompany(1);

            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isEqualTo("employee@example.com");
            assertThat(result.get(0).getCompanyId()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should return empty list when no employees exist")
        void getEmployeesByCompany_EmptyList() {
            when(companyEmployeeRepo.findAllUsersByCompanyId(1)).thenReturn(new ArrayList<>());

            List<UserDTO> result = companyEmployeeService.getEmployeesByCompany(1);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return multiple employees")
        void getEmployeesByCompany_MultipleEmployees() {
            User secondUser = new User();
            secondUser.setId(2);
            secondUser.setEmail("employee2@example.com");
            secondUser.setCompany(testCompany);

            UserDTO secondUserDTO = new UserDTO();
            secondUserDTO.setId(2);
            secondUserDTO.setEmail("employee2@example.com");
            secondUserDTO.setCompanyId(1);

            when(companyEmployeeRepo.findAllUsersByCompanyId(1))
                    .thenReturn(Arrays.asList(testUser, secondUser));
            when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);
            when(userMapper.toDTO(secondUser)).thenReturn(secondUserDTO);

            List<UserDTO> result = companyEmployeeService.getEmployeesByCompany(1);

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("AddUserToCompany Tests")
    class AddUserToCompanyTests {

        @Test
        @DisplayName("Should add user to company successfully")
        void addUserToCompany_Success() {
            when(userRepo.findById(1)).thenReturn(Optional.of(testUser));
            when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
            when(companyRoleRepo.findById(1)).thenReturn(Optional.of(testRole));
            when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                return user;
            });
            when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);

            UserDTO result = companyEmployeeService.addUserToCompany(addRequest);

            assertThat(result).isNotNull();
            assertThat(result.getCompanyId()).isEqualTo(1);
            verify(userRepo).save(argThat(user ->
                    user.getCompany() != null &&
                    user.getCompanyRole() != null
            ));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when user not found")
        void addUserToCompany_ThrowsException_WhenUserNotFound() {
            when(userRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyEmployeeService.addUserToCompany(addRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("User not found with id: 1");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when company not found")
        void addUserToCompany_ThrowsException_WhenCompanyNotFound() {
            when(userRepo.findById(1)).thenReturn(Optional.of(testUser));
            when(companyRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyEmployeeService.addUserToCompany(addRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Company not found with id: 1");
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when role not found")
        void addUserToCompany_ThrowsException_WhenRoleNotFound() {
            when(userRepo.findById(1)).thenReturn(Optional.of(testUser));
            when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
            when(companyRoleRepo.findById(1)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyEmployeeService.addUserToCompany(addRequest))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Company role not found with id: 1");
        }

        @Test
        @DisplayName("Should throw IllegalStateException when user already belongs to company")
        void addUserToCompany_ThrowsException_WhenUserAlreadyInCompany() {
            testUser.setCompany(testCompany);

            when(userRepo.findById(1)).thenReturn(Optional.of(testUser));
            when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
            when(companyRoleRepo.findById(1)).thenReturn(Optional.of(testRole));

            assertThatThrownBy(() -> companyEmployeeService.addUserToCompany(addRequest))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("The user already belongs to the company with id: 1");
        }

        @Test
        @DisplayName("Should allow adding user from different company")
        void addUserToCompany_Success_WhenUserFromDifferentCompany() {
            Company otherCompany = new Company();
            otherCompany.setId(2);
            otherCompany.setName("Other Company");
            testUser.setCompany(otherCompany);

            when(userRepo.findById(1)).thenReturn(Optional.of(testUser));
            when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
            when(companyRoleRepo.findById(1)).thenReturn(Optional.of(testRole));
            when(userRepo.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);

            UserDTO result = companyEmployeeService.addUserToCompany(addRequest);

            assertThat(result).isNotNull();
            verify(userRepo).save(any(User.class));
        }
    }
}
