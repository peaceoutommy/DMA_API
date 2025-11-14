package dev.tomas.dma;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    private Company testCompany;
    private Company otherCompany;
    private CompanyRole testRole;
    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        // Setup test company
        testCompany = new Company();
        testCompany.setId(1);
        testCompany.setName("Test Company");
        testCompany.setRegistrationNumber("REG123");
        testCompany.setTaxId("TAX123");

        // Setup other company
        otherCompany = new Company();
        otherCompany.setId(2);
        otherCompany.setName("Other Company");
        otherCompany.setRegistrationNumber("REG456");
        otherCompany.setTaxId("TAX456");

        // Setup test role
        testRole = new CompanyRole();
        testRole.setId(1);
        testRole.setName("Employee");
        testRole.setCompany(testCompany);

        // Setup test user
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@test.com");
        testUser.setUsername("testuser");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhoneNumber("1234567890");
        testUser.setAddress("123 Test St");
        testUser.setCompany(testCompany);
        testUser.setCompanyRole(testRole);

        // Setup test DTO
        testUserDTO = new UserDTO();
        testUserDTO.setId(1);
        testUserDTO.setEmail("test@test.com");
        testUserDTO.setUsername("testuser");
        testUserDTO.setFirstName("John");
        testUserDTO.setLastName("Doe");
        testUserDTO.setPhoneNumber("1234567890");
        testUserDTO.setAddress("123 Test St");
        testUserDTO.setCompanyId(1);
        testUserDTO.setCompanyRoleId(1);
        testUserDTO.setCompanyRoleName("Employee");
    }

    @Test
    void getEmployeesByCompany_ShouldReturnListOfEmployees() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(companyEmployeeRepo.findAllUsersByCompanyId(1)).thenReturn(users);
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

        // Act
        List<UserDTO> result = companyEmployeeService.getEmployeesByCompany(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test@test.com", result.get(0).getEmail());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("John", result.get(0).getFirstName());
        assertEquals("Doe", result.get(0).getLastName());
        verify(companyEmployeeRepo, times(1)).findAllUsersByCompanyId(1);
        verify(userMapper, times(1)).toDTO(testUser);
    }

    @Test
    void getEmployeesByCompany_ShouldReturnEmptyList_WhenNoEmployees() {
        // Arrange
        when(companyEmployeeRepo.findAllUsersByCompanyId(1)).thenReturn(Collections.emptyList());

        // Act
        List<UserDTO> result = companyEmployeeService.getEmployeesByCompany(1);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(companyEmployeeRepo, times(1)).findAllUsersByCompanyId(1);
        verify(userMapper, never()).toDTO(any(User.class));
    }

    @Test
    void getEmployeesByCompany_ShouldReturnMultipleEmployees() {
        // Arrange
        User user2 = new User();
        user2.setId(2);
        user2.setEmail("user2@test.com");
        user2.setCompany(testCompany);

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(2);
        userDTO2.setEmail("user2@test.com");

        List<User> users = Arrays.asList(testUser, user2);
        when(companyEmployeeRepo.findAllUsersByCompanyId(1)).thenReturn(users);
        when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);
        when(userMapper.toDTO(user2)).thenReturn(userDTO2);

        // Act
        List<UserDTO> result = companyEmployeeService.getEmployeesByCompany(1);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(companyEmployeeRepo, times(1)).findAllUsersByCompanyId(1);
        verify(userMapper, times(2)).toDTO(any(User.class));
    }

    @Test
    void addUserToCompany_ShouldAddUserSuccessfully() {
        // Arrange
        User userWithoutCompany = new User();
        userWithoutCompany.setId(2);
        userWithoutCompany.setEmail("newuser@test.com");
        userWithoutCompany.setCompany(otherCompany);

        AddUserToCompanyReq request = new AddUserToCompanyReq();
        request.setEmployeeId(2);
        request.setCompanyId(1);
        request.setRoleId(1);

        when(userRepo.findById(2)).thenReturn(Optional.of(userWithoutCompany));
        when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
        when(companyRoleRepo.findById(1)).thenReturn(Optional.of(testRole));
        when(userRepo.save(any(User.class))).thenReturn(userWithoutCompany);
        when(userMapper.toDTO(userWithoutCompany)).thenReturn(testUserDTO);

        // Act
        UserDTO result = companyEmployeeService.addUserToCompany(request);

        // Assert
        assertNotNull(result);
        verify(userRepo, times(1)).findById(2);
        verify(companyRepo, times(1)).findById(1);
        verify(companyRoleRepo, times(1)).findById(1);
        verify(userRepo, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toDTO(any(User.class));
    }

    @Test
    void addUserToCompany_ShouldThrowEntityNotFoundException_WhenUserNotFound() {
        // Arrange
        AddUserToCompanyReq request = new AddUserToCompanyReq();
        request.setEmployeeId(999);
        request.setCompanyId(1);
        request.setRoleId(1);

        when(userRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> companyEmployeeService.addUserToCompany(request));
        verify(userRepo, times(1)).findById(999);
        verify(companyRepo, never()).findById(any());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void addUserToCompany_ShouldThrowEntityNotFoundException_WhenCompanyNotFound() {
        // Arrange
        AddUserToCompanyReq request = new AddUserToCompanyReq();
        request.setEmployeeId(1);
        request.setCompanyId(999);
        request.setRoleId(1);

        when(userRepo.findById(1)).thenReturn(Optional.of(testUser));
        when(companyRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> companyEmployeeService.addUserToCompany(request));
        verify(userRepo, times(1)).findById(1);
        verify(companyRepo, times(1)).findById(999);
        verify(companyRoleRepo, never()).findById(any());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void addUserToCompany_ShouldThrowEntityNotFoundException_WhenRoleNotFound() {
        // Arrange
        AddUserToCompanyReq request = new AddUserToCompanyReq();
        request.setEmployeeId(1);
        request.setCompanyId(1);
        request.setRoleId(999);

        when(userRepo.findById(1)).thenReturn(Optional.of(testUser));
        when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
        when(companyRoleRepo.findById(999)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> companyEmployeeService.addUserToCompany(request));
        verify(userRepo, times(1)).findById(1);
        verify(companyRepo, times(1)).findById(1);
        verify(companyRoleRepo, times(1)).findById(999);
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void addUserToCompany_ShouldThrowIllegalStateException_WhenUserAlreadyBelongsToCompany() {
        // Arrange
        AddUserToCompanyReq request = new AddUserToCompanyReq();
        request.setEmployeeId(1);
        request.setCompanyId(1);
        request.setRoleId(1);

        when(userRepo.findById(1)).thenReturn(Optional.of(testUser));
        when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
        when(companyRoleRepo.findById(1)).thenReturn(Optional.of(testRole));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> companyEmployeeService.addUserToCompany(request)
        );
        assertTrue(exception.getMessage().contains("already belongs to the company"));
        verify(userRepo, times(1)).findById(1);
        verify(companyRepo, times(1)).findById(1);
        verify(companyRoleRepo, times(1)).findById(1);
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void addUserToCompany_ShouldSetCorrectCompanyAndRole() {
        // Arrange
        User userToAdd = new User();
        userToAdd.setId(3);
        userToAdd.setEmail("newemployee@test.com");
        userToAdd.setCompany(otherCompany); // User belongs to a different company

        AddUserToCompanyReq request = new AddUserToCompanyReq();
        request.setEmployeeId(3);
        request.setCompanyId(1);
        request.setRoleId(1);

        when(userRepo.findById(3)).thenReturn(Optional.of(userToAdd));
        when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
        when(companyRoleRepo.findById(1)).thenReturn(Optional.of(testRole));
        when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            assertEquals(testCompany, savedUser.getCompany());
            assertEquals(testRole, savedUser.getCompanyRole());
            return savedUser;
        });
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);

        // Act
        UserDTO result = companyEmployeeService.addUserToCompany(request);

        // Assert
        assertNotNull(result);
        verify(userRepo, times(1)).save(any(User.class));
    }

    @Test
    void addUserToCompany_ShouldSuccessfullyAddUserWithNullCompany() {
        // Arrange
        User userWithoutCompany = new User();
        userWithoutCompany.setId(4);
        userWithoutCompany.setEmail("orphan@test.com");
        userWithoutCompany.setCompany(null); // User doesn't belong to any company yet

        AddUserToCompanyReq request = new AddUserToCompanyReq();
        request.setEmployeeId(4);
        request.setCompanyId(1);
        request.setRoleId(1);

        when(userRepo.findById(4)).thenReturn(Optional.of(userWithoutCompany));
        when(companyRepo.findById(1)).thenReturn(Optional.of(testCompany));
        when(companyRoleRepo.findById(1)).thenReturn(Optional.of(testRole));
        when(userRepo.save(any(User.class))).thenReturn(userWithoutCompany);
        when(userMapper.toDTO(userWithoutCompany)).thenReturn(testUserDTO);

        // Act
        UserDTO result = companyEmployeeService.addUserToCompany(request);

        // Assert
        assertNotNull(result);
        verify(userRepo, times(1)).findById(4);
        verify(companyRepo, times(1)).findById(1);
        verify(companyRoleRepo, times(1)).findById(1);
        verify(userRepo, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toDTO(any(User.class));
    }
}