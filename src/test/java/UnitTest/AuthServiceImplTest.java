package UnitTest;

import dev.tomas.dma.dto.request.AuthReq;
import dev.tomas.dma.dto.request.UserRegisterReq;
import dev.tomas.dma.dto.response.AuthRes;
import dev.tomas.dma.dto.response.AuthUserRes;
import dev.tomas.dma.entity.Company;
import dev.tomas.dma.entity.CompanyRole;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.enums.UserRole;
import dev.tomas.dma.repository.UserRepo;
import dev.tomas.dma.service.JWTService;
import dev.tomas.dma.service.implementation.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepo userRepo;

    @Mock
    private JWTService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserRegisterReq registerRequest;
    private AuthReq authReq;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhoneNumber("1234567890");
        testUser.setAddress("123 Test St");
        testUser.setRole(UserRole.DONOR);

        registerRequest = new UserRegisterReq();
        registerRequest.setEmail("new@example.com");
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Jane");
        registerRequest.setLastName("Doe");
        registerRequest.setPhoneNumber("0987654321");
        registerRequest.setAddress("456 New St");
        registerRequest.setMiddleNames("Middle");
        registerRequest.setCompanyAccount(false);

        authReq = new AuthReq();
        authReq.setEmail("test@example.com");
        authReq.setPassword("password123");
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully as DONOR")
        void register_Success_AsDonor() {
            when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userRepo.findByUsername(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1);
                return user;
            });
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

            AuthRes result = authService.register(registerRequest);

            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo("jwt-token");
            assertThat(result.getUser()).isNotNull();
            assertThat(result.getUser().getEmail()).isEqualTo("new@example.com");

            verify(userRepo).save(any(User.class));
            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("Should register new user successfully as COMPANY_ACCOUNT")
        void register_Success_AsCompanyAccount() {
            registerRequest.setCompanyAccount(true);

            when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userRepo.findByUsername(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userRepo.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1);
                return user;
            });
            when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

            AuthRes result = authService.register(registerRequest);

            assertThat(result).isNotNull();
            verify(userRepo).save(argThat(user -> user.getRole() == UserRole.COMPANY_ACCOUNT));
        }

        @Test
        @DisplayName("Should throw DuplicateKeyException when email already exists")
        void register_ThrowsException_WhenEmailExists() {
            when(userRepo.findByEmail("new@example.com")).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(DuplicateKeyException.class)
                    .hasMessage("Email already exists");

            verify(userRepo, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw DuplicateKeyException when username already exists")
        void register_ThrowsException_WhenUsernameExists() {
            when(userRepo.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userRepo.findByUsername("newuser")).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(DuplicateKeyException.class)
                    .hasMessage("Username already exists");

            verify(userRepo, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with email")
        void login_Success_WithEmail() {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(jwtService.generateToken(testUser)).thenReturn("jwt-token");

            AuthRes result = authService.login(authReq);

            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo("jwt-token");
            assertThat(result.getUser().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should login successfully with username")
        void login_Success_WithUsername() {
            authReq.setEmail(null);
            authReq.setUsername("testuser");

            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(jwtService.generateToken(testUser)).thenReturn("jwt-token");

            AuthRes result = authService.login(authReq);

            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo("jwt-token");
        }

        @Test
        @DisplayName("Should login successfully with company role")
        void login_Success_WithCompanyRole() {
            Company company = new Company();
            company.setId(1);
            company.setName("Test Company");

            CompanyRole companyRole = new CompanyRole();
            companyRole.setId(1);
            companyRole.setName("Manager");
            companyRole.setCompany(company);

            testUser.setCompanyRole(companyRole);
            testUser.setCompany(company);

            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(jwtService.generateToken(testUser)).thenReturn("jwt-token");

            AuthRes result = authService.login(authReq);

            assertThat(result).isNotNull();
            assertThat(result.getUser().getCompanyId()).isEqualTo(1);
            assertThat(result.getUser().getCompanyRole()).isEqualTo("Manager");
        }

        @Test
        @DisplayName("Should throw BadCredentialsException for invalid credentials")
        void login_ThrowsException_InvalidCredentials() {
            when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(authReq))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid username/email or password");
        }
    }

    @Nested
    @DisplayName("AuthMe Tests")
    class AuthMeTests {

        @Test
        @DisplayName("Should return current user info")
        void authMe_Success() {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(testUser);

            AuthUserRes result = authService.authMe(authentication);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getRole()).isEqualTo("DONOR");
        }

        @Test
        @DisplayName("Should return current user info with company role")
        void authMe_Success_WithCompanyRole() {
            Company company = new Company();
            company.setId(1);

            CompanyRole companyRole = new CompanyRole();
            companyRole.setId(1);
            companyRole.setName("Admin");
            companyRole.setCompany(company);

            testUser.setCompanyRole(companyRole);

            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(testUser);

            AuthUserRes result = authService.authMe(authentication);

            assertThat(result.getCompanyId()).isEqualTo(1);
            assertThat(result.getCompanyRole()).isEqualTo("Admin");
        }
    }

    @Nested
    @DisplayName("LoadUserByUsername Tests")
    class LoadUserByUsernameTests {

        @Test
        @DisplayName("Should load user by username")
        void loadUserByUsername_Success_ByUsername() {
            when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

            var result = authService.loadUserByUsername("testuser");

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("test@example.com"); // Returns email as username
        }

        @Test
        @DisplayName("Should load user by email when username not found")
        void loadUserByUsername_Success_ByEmail() {
            when(userRepo.findByUsername("test@example.com")).thenReturn(Optional.empty());
            when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            var result = authService.loadUserByUsername("test@example.com");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when user not found")
        void loadUserByUsername_ThrowsException_WhenNotFound() {
            when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());
            when(userRepo.findByEmail("unknown")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.loadUserByUsername("unknown"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("User not found with username or email: unknown");
        }
    }
}
