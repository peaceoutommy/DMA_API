package dev.tomas.dma.service;

import dev.tomas.dma.dto.request.AuthReq;
import dev.tomas.dma.dto.response.AuthRes;
import dev.tomas.dma.dto.response.AuthUserRes;
import dev.tomas.dma.dto.request.UserRegisterReq;
import org.springframework.security.core.Authentication;

public interface AuthService {
    AuthRes login(AuthReq authReq);
    AuthRes register(UserRegisterReq registerRequest);
    AuthUserRes authMe(Authentication authentication);
}
