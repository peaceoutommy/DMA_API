package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.AuthResponse;
import dev.tomas.dma.dto.AuthUserResponse;
import dev.tomas.dma.dto.UserRegisterRequest;
import dev.tomas.dma.dto.AuthRequest;
import dev.tomas.dma.mapper.AuthResponseMapper;
import dev.tomas.dma.model.entity.UserEntity;
import dev.tomas.dma.repository.AuthRepo;
import dev.tomas.dma.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService, UserDetailsService {
    private final AuthRepo authRepo;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;

    public AuthServiceImpl(AuthRepo authRepo,
                           JWTService jwtService,
                           PasswordEncoder passwordEncoder,
                           @Lazy AuthenticationManager authManager
    ) {
        this.authRepo = authRepo;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
    }

    @Override
    public AuthResponse register(UserRegisterRequest registerRequest) {

        if (registerRequest.getEmail() != null && authRepo.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (registerRequest.getUsername() != null && authRepo.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        UserEntity entity = new UserEntity();
        entity.setEmail(registerRequest.getEmail());
        entity.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        entity.setPhoneNumber(registerRequest.getPhoneNumber());
        entity.setAddress(registerRequest.getAddress());
        entity.setFirstName(registerRequest.getFirstName());
        entity.setLastName(registerRequest.getLastName());
        entity.setMiddleNames(registerRequest.getMiddleNames());
        entity.setUsername(registerRequest.getUsername());

        var createdUser = authRepo.save(entity);

        AuthResponse response = new AuthResponse();
        response.setToken(jwtService.generateToken(createdUser));
        response.setUser(AuthResponseMapper.INSTANCE.convertToModel(createdUser));
        return response;
    }

    @Override
    public AuthResponse login(AuthRequest authRequest) {
        try {
            UsernamePasswordAuthenticationToken authRequestToken =
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername() == null ? authRequest.getEmail() : authRequest.getUsername(), authRequest.getPassword());

            Authentication authentication = authManager.authenticate(authRequestToken);
            UserEntity user = (UserEntity) authentication.getPrincipal();

            AuthResponse response = new AuthResponse();
            response.setToken(jwtService.generateToken(user));
            response.setUser(AuthResponseMapper.INSTANCE.convertToModel(user));

            return response;

        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid username/email or password");
        } catch (AuthenticationException e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    public AuthUserResponse authMe(Authentication authentication) {
        String username = authentication.getName();
        UserEntity user = authRepo.findByUsername(username)
                .or(() -> authRepo.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));

        AuthUserResponse response = new AuthUserResponse();
        return AuthResponseMapper.INSTANCE.convertToModel(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return authRepo.findByUsername(username)
                .or(() -> authRepo.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));
    }
}

