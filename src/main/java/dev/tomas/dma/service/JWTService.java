package dev.tomas.dma.service;

import dev.tomas.dma.entity.User;
import io.jsonwebtoken.Claims;

import java.util.Date;
import java.util.function.Function;

public interface JWTService {
    String extractUsername(String token);
    Date extractExpiration(String token);
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
    String generateToken(User user);
    Boolean validateToken(String token, User user);
}
