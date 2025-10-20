package dev.tomas.dma.service.implementation;

import dev.tomas.dma.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@NoArgsConstructor
@Component
public class JWTService {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;

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
                .verifyWith(getSigningKey()) // Verify signature with our secret key
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
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, user.getUsername());
    }

    /**
     * Generate JWT token with extra custom claims
     * Example extra claims: roles, permissions, user ID, etc.
     */
    public String generateToken(Map<String, Object> extraClaims, User userDetails) {
        return createToken(extraClaims, userDetails.getUsername());
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
    public Boolean validateToken(String token, User user) {
        final String username = extractUsername(token);
        return (username.equals(user.getUsername()) && !isTokenExpired(token));
    }
}