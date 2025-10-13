package dev.tomas.dma.controller;

import dev.tomas.dma.dto.AuthReq;
import dev.tomas.dma.dto.AuthRes;
import dev.tomas.dma.dto.AuthUserResponse;
import dev.tomas.dma.dto.UserRegisterReq;
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
    public AuthRes register(@RequestBody @Valid UserRegisterReq registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping("login")
    public AuthRes login(@RequestBody @Valid AuthReq authReq) {
        return authService.login(authReq);
    }

    @GetMapping("me")
    public AuthUserResponse getCurrentUser(Authentication authentication) {
        return authService.authMe(authentication);
    }
}
