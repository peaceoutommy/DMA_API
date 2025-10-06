package dev.tomas.dma.service;

import dev.tomas.dma.dto.AuthRequest;
import dev.tomas.dma.dto.AuthResponse;
import dev.tomas.dma.dto.AuthUserResponse;
import dev.tomas.dma.dto.UserRegisterRequest;
import dev.tomas.dma.model.Company;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);
    AuthResponse register(UserRegisterRequest registerRequest);
    AuthUserResponse authMe(Authentication authentication);
    UserDetails loadUserByUsername(String username);
}
