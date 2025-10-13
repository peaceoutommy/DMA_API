package dev.tomas.dma.service;

import dev.tomas.dma.dto.AuthReq;
import dev.tomas.dma.dto.AuthRes;
import dev.tomas.dma.dto.AuthUserResponse;
import dev.tomas.dma.dto.UserRegisterReq;
import org.springframework.security.core.Authentication;

public interface AuthService {
    AuthRes login(AuthReq authReq);
    AuthRes register(UserRegisterReq registerRequest);
    AuthUserResponse authMe(Authentication authentication);
}
