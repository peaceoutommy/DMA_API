package dev.tomas.dma.service;

import dev.tomas.dma.dto.AuthRequest;
import dev.tomas.dma.dto.AuthResponse;
import dev.tomas.dma.dto.UserRegisterRequest;
import dev.tomas.dma.model.Company;

public interface AuthService {
    AuthResponse login(AuthRequest authRequest);
    AuthResponse register(UserRegisterRequest registerRequest);
}
