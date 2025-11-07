package dev.tomas.dma;

import dev.tomas.dma.dto.request.AuthReq;
import dev.tomas.dma.dto.request.UserRegisterReq;
import dev.tomas.dma.dto.response.AuthRes;
import dev.tomas.dma.dto.response.AuthUserRes;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.repository.UserRepo;
import dev.tomas.dma.service.implementation.AuthServiceImpl;
import dev.tomas.dma.service.implementation.JWTServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private UserRepo userRepo;

    @Mock
    private JWTServiceImpl jwtServiceImpl;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private UserRegisterReq registerRequest;
    private AuthReq authReq;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@testuser.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setPhoneNumber("1234567890");
        testUser.setAddress("123 Test St");

        registerRequest = new UserRegisterReq();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@request.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setPhoneNumber("1234567890");
        registerRequest.setAddress("123 Test St");

        authReq = new AuthReq();
        authReq.setUsername("testuser");
        authReq.setPassword("password123");
    }


    @Test
    void testRegister_success() {
        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepo.findByUsername(registerRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepo.save(any(User.class))).thenReturn(testUser);
        when(jwtServiceImpl.generateToken(testUser)).thenReturn("jwt-token");

        AuthRes result = authService.register(registerRequest);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertNotNull(result.getUser());
        verify(userRepo).save(any(User.class));
        verify(passwordEncoder).encode(registerRequest.getPassword());
    }

    @Test
    void testRegister_emailAlreadyExists() {
        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(registerRequest));

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }

    @Test
    void testRegister_usernameAlreadyExists() {
        when(userRepo.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepo.findByUsername(registerRequest.getUsername())).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.register(registerRequest));

        assertEquals("Username already exists", exception.getMessage());
        verify(userRepo, never()).save(any(User.class));
    }


    @Test
    void testLogin_withUsername_success() {
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(jwtServiceImpl.generateToken(testUser)).thenReturn("jwt-token");

        AuthRes result = authService.login(authReq);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertNotNull(result.getUser());
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testLogin_withEmail_success() {
        authReq.setUsername(null);
        authReq.setEmail("test@example.com");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(jwtServiceImpl.generateToken(testUser)).thenReturn("jwt-token");

        AuthRes result = authService.login(authReq);

        assertNotNull(result);
        assertEquals("jwt-token", result.getToken());
        assertNotNull(result.getUser());
    }

    @Test
    void testLogin_badCredentials() {
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(authReq));

        assertEquals("Invalid username/email or password", exception.getMessage());
    }

    @Test
    void testLogin_authenticationFailed() {
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Authentication error"));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.login(authReq));

        assertTrue(exception.getMessage().contains("Invalid username/email or password"));
    }


    @Test
    void testAuthMe_foundByUsername() {
        when(authentication.getName()).thenReturn("testuser");
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        AuthUserRes result = authService.authMe(authentication);

        assertNotNull(result);
        verify(userRepo).findByUsername("testuser");
    }

    @Test
    void testAuthMe_foundByEmail() {
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepo.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        AuthUserRes result = authService.authMe(authentication);

        assertNotNull(result);
        verify(userRepo).findByUsername("test@example.com");
        verify(userRepo).findByEmail("test@example.com");
    }

    @Test
    void testAuthMe_userNotFound() {
        when(authentication.getName()).thenReturn("nonexistent");
        when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> authService.authMe(authentication));
    }


    @Test
    void testLoadUserByUsername_foundByUsername() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UserDetails result = authService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals(testUser, result);
        verify(userRepo).findByUsername("testuser");
    }

    @Test
    void testLoadUserByUsername_foundByEmail() {
        when(userRepo.findByUsername("test@example.com")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UserDetails result = authService.loadUserByUsername("test@example.com");

        assertNotNull(result);
        assertEquals(testUser, result);
        verify(userRepo).findByUsername("test@example.com");
        verify(userRepo).findByEmail("test@example.com");
    }

    @Test
    void testLoadUserByUsername_notFound() {
        when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());
        when(userRepo.findByEmail("nonexistent")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> authService.loadUserByUsername("nonexistent"));
    }
}