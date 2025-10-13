package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.AuthRes;
import dev.tomas.dma.dto.AuthUserResponse;
import dev.tomas.dma.dto.UserRegisterReq;
import dev.tomas.dma.dto.AuthReq;
import dev.tomas.dma.mapper.AuthResponseMapper;
import dev.tomas.dma.model.entity.UserEntity;
import dev.tomas.dma.repository.AuthRepo;
import dev.tomas.dma.service.AuthService;
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
    public AuthRes register(UserRegisterReq registerRequest) {

        if (registerRequest.getEmail() != null && authRepo.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        if (registerRequest.getUsername() != null && authRepo.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        UserEntity toSave = new UserEntity();
        toSave.setEmail(registerRequest.getEmail());
        toSave.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        toSave.setPhoneNumber(registerRequest.getPhoneNumber());
        toSave.setAddress(registerRequest.getAddress());
        toSave.setFirstName(registerRequest.getFirstName());
        toSave.setLastName(registerRequest.getLastName());
        toSave.setMiddleNames(registerRequest.getMiddleNames());
        toSave.setUsername(registerRequest.getUsername());

        UserEntity createdUser = authRepo.save(toSave);
        return new AuthRes(jwtService.generateToken(createdUser), AuthResponseMapper.INSTANCE.convertToModel(createdUser));
    }

    @Override
    public AuthRes login(AuthReq authReq) {
        try {
            UsernamePasswordAuthenticationToken authRequestToken =
                    new UsernamePasswordAuthenticationToken(authReq.getUsername() == null ? authReq.getEmail() : authReq.getUsername(), authReq.getPassword());

            Authentication authentication = authManager.authenticate(authRequestToken);
            UserEntity user = (UserEntity) authentication.getPrincipal();

            return new AuthRes(jwtService.generateToken(user), AuthResponseMapper.INSTANCE.convertToModel(user));

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

