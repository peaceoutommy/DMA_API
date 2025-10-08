package dev.tomas.dma.controller;

import dev.tomas.dma.dto.AuthRequest;
import dev.tomas.dma.dto.AuthResponse;
import dev.tomas.dma.dto.AuthUserResponse;
import dev.tomas.dma.dto.UserRegisterRequest;
import dev.tomas.dma.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("me")
    public AuthUserResponse getCurrentUser(Authentication authentication) {
        return authService.authMe(authentication);
    }
}
