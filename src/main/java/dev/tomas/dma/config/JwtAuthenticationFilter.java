package dev.tomas.dma.config;

import dev.tomas.dma.entity.User;
import dev.tomas.dma.service.implementation.JWTService;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JWTService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * This filter intercepts EVERY incoming HTTP request BEFORE it reaches the controllers.
     * Its job: Check if the request has a valid JWT and authenticate the user.
     */
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain chain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register")) {
            chain.doFilter(request, response);
            return;
        }

        // Step 1: Extract the "Authorization" header from the request
        // Expected format: "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Step 2: If no Authorization header OR it doesn't start with "Bearer ", skip authentication
        // This allows public endpoints (like /api/auth/login) to work without a token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response); // Continue to next filter/controller
            return;
        }

        // Step 3: Extract the actual JWT token (remove "Bearer " prefix - 7 characters)
        jwt = authHeader.substring(7);

        // Step 4: Extract username from the JWT token (stored in "subject" claim)
        username = jwtService.extractUsername(jwt);

        // Step 5: If username exists AND user is not already authenticated in this request
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Step 6: Load the full user details from database using UserDetailsService
            User user = (User) userDetailsService.loadUserByUsername(username);

            // Step 7: Validate that the token is valid (not expired, matches the user)
            if (jwtService.validateToken(jwt, user)) {
                // Step 8: Create an authentication token with the user's details and authorities
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                // Step 9: Add request details (IP address, session ID, etc.)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 10: Set the authentication in Spring Security context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Step 11: Continue to the next filter or controller
        chain.doFilter(request, response);
    }
}