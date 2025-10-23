package dev.tomas.dma.service.implementation;

import dev.tomas.dma.dto.response.MembershipGetRes;
import dev.tomas.dma.entity.User;
import dev.tomas.dma.model.UserModel;
import dev.tomas.dma.service.CompanyService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class JWTService {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;
    private final CompanyService companyService;

    /**
     * Creates a cryptographic signing key from the secret string
     * Used to sign and verify JWT tokens
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Extract username from JWT token
     * The username is stored in the "subject" claim
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from JWT token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract any claim from the token
     * Uses a function to specify which claim to extract
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse the JWT token and extract all claims
     * This verifies the token signature using our secret key
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token) // Parse the token
                .getPayload(); // Get the claims (payload)
    }

    /**
     * Check if the token has expired
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generate JWT token with no extra claims
     * Just username and standard claims (issued at, expiration)
     */
    public String generateToken(UserModel user) {
        Map<String, Object> claims = new HashMap<>();

        if (user != null && user.getId() != null) {
            var membership = companyService.getMembershipByUserId(user.getId());
            if (membership.isPresent()) {
                claims.put("Company", membership.get().getCompanyId());
                claims.put("Role", membership.get().getCompanyRole());
            };
        }
        return createToken(claims, user.getUsername());
    }

    /**
     * Create the actual JWT token
     * Structure: Header.Payload.Signature
     * - Header: algorithm and type
     * - Payload: claims (username, expiration, custom data)
     * - Signature: verifies token wasn't tampered with
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims) // Add custom claims
                .subject(subject) // Set username as subject
                .issuedAt(new Date(System.currentTimeMillis())) // Token creation time
                .expiration(new Date(System.currentTimeMillis() + expiration)) // Token expiration
                .signWith(getSigningKey()) // Sign with our secret key
                .compact(); // Build the token string
    }

    /**
     * Validate JWT token
     * Checks:
     * 1. Username in token matches the user
     * 2. Token hasn't expired
     */
    public Boolean validateToken(String token, UserModel user) {
        final String username = extractUsername(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }
}