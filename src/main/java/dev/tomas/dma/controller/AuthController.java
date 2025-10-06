package dev.tomas.dma.controller;

import dev.tomas.dma.dto.AuthRequest;
import dev.tomas.dma.dto.AuthResponse;
import dev.tomas.dma.dto.UserRegisterRequest;
import dev.tomas.dma.service.AuthService;
import dev.tomas.dma.service.implementation.JWTService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("register")
    public AuthResponse register(@RequestBody @Valid UserRegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping("login")
    public AuthResponse login(@RequestBody @Valid AuthRequest authRequest) {
        return authService.login(authRequest);
    }
}
