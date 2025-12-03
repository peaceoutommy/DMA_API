package dev.tomas.dma.service.implementation;

import dev.tomas.dma.entity.Company;
import dev.tomas.dma.entity.CompanyRole;
import dev.tomas.dma.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JWTServiceImplTest {

    private JWTServiceImpl jwtService;

    private User testUser;
    private Company testCompany;
    private CompanyRole testRole;

    private static final String SECRET = "thisIsASecretKeyForTestingPurposesOnlyMustBeLongEnough";
    private static final Long EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JWTServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);

        testCompany = new Company();
        testCompany.setId(1);
        testCompany.setName("Test Company");

        testRole = new CompanyRole();
        testRole.setId(1);
        testRole.setName("Manager");
        testRole.setCompany(testCompany);

        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPassword("password");
    }

    @Nested
    @DisplayName("GenerateToken Tests")
    class GenerateTokenTests {

        @Test
        @DisplayName("Should generate token for user without company")
        void generateToken_Success_WithoutCompany() {
            String token = jwtService.generateToken(testUser);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
        }

        @Test
        @DisplayName("Should generate token for user with company and role")
        void generateToken_Success_WithCompanyRole() {
            testUser.setCompany(testCompany);
            testUser.setCompanyRole(testRole);

            String token = jwtService.generateToken(testUser);

            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user is null")
        void generateToken_ThrowsException_WhenUserNull() {
            assertThatThrownBy(() -> jwtService.generateToken(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User can't be null");
        }
    }

    @Nested
    @DisplayName("ExtractUsername Tests")
    class ExtractUsernameTests {

        @Test
        @DisplayName("Should extract username from token")
        void extractUsername_Success() {
            String token = jwtService.generateToken(testUser);

            String username = jwtService.extractUsername(token);

            assertThat(username).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("ExtractExpiration Tests")
    class ExtractExpirationTests {

        @Test
        @DisplayName("Should extract expiration date from token")
        void extractExpiration_Success() {
            String token = jwtService.generateToken(testUser);

            Date expiration = jwtService.extractExpiration(token);

            assertThat(expiration).isNotNull();
            assertThat(expiration).isAfter(new Date());
        }

        @Test
        @DisplayName("Expiration should be approximately 1 hour from now")
        void extractExpiration_ApproximatelyOneHourFromNow() {
            String token = jwtService.generateToken(testUser);

            Date expiration = jwtService.extractExpiration(token);
            Date expectedExpiration = new Date(System.currentTimeMillis() + EXPIRATION);

            // Allow 5 second variance
            assertThat(Math.abs(expiration.getTime() - expectedExpiration.getTime())).isLessThan(5000);
        }
    }

    @Nested
    @DisplayName("ExtractClaim Tests")
    class ExtractClaimTests {

        @Test
        @DisplayName("Should extract subject claim")
        void extractClaim_Subject_Success() {
            String token = jwtService.generateToken(testUser);

            String subject = jwtService.extractClaim(token, Claims::getSubject);

            assertThat(subject).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should extract issued at claim")
        void extractClaim_IssuedAt_Success() {
            String token = jwtService.generateToken(testUser);

            Date issuedAt = jwtService.extractClaim(token, Claims::getIssuedAt);

            assertThat(issuedAt).isNotNull();
            assertThat(issuedAt).isBeforeOrEqualTo(new Date());
        }
    }

    @Nested
    @DisplayName("ValidateToken Tests")
    class ValidateTokenTests {

        @Test
        @DisplayName("Should validate token successfully")
        void validateToken_Success() {
            String token = jwtService.generateToken(testUser);

            Boolean isValid = jwtService.validateToken(token, testUser);

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should return false for wrong user")
        void validateToken_Failure_WrongUser() {
            String token = jwtService.generateToken(testUser);

            User differentUser = new User();
            differentUser.setUsername("differentuser");

            Boolean isValid = jwtService.validateToken(token, differentUser);

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Should return false for expired token")
        void validateToken_Failure_ExpiredToken() {
            // Create a service with very short expiration
            JWTServiceImpl shortExpirationService = new JWTServiceImpl();
            ReflectionTestUtils.setField(shortExpirationService, "secret", SECRET);
            ReflectionTestUtils.setField(shortExpirationService, "expiration", 1L); // 1 millisecond

            String token = shortExpirationService.generateToken(testUser);

            // Wait for token to expire
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            assertThatThrownBy(() -> shortExpirationService.validateToken(token, testUser))
                    .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
        }
    }

    @Nested
    @DisplayName("Token Claims Tests")
    class TokenClaimsTests {

        @Test
        @DisplayName("Should include company id in claims when user has company")
        void tokenClaims_IncludesCompanyId() {
            testUser.setCompany(testCompany);
            testUser.setCompanyRole(testRole);

            String token = jwtService.generateToken(testUser);
            SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            assertThat(claims.get("Company")).isEqualTo(1);
        }

        @Test
        @DisplayName("Should include role name in claims when user has company role")
        void tokenClaims_IncludesRoleName() {
            testUser.setCompany(testCompany);
            testUser.setCompanyRole(testRole);

            String token = jwtService.generateToken(testUser);
            SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            assertThat(claims.get("Role")).isEqualTo("Manager");
        }

        @Test
        @DisplayName("Should not include company claims when user has no company")
        void tokenClaims_NoCompanyClaims_WhenNoCompany() {
            String token = jwtService.generateToken(testUser);
            SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            assertThat(claims.get("Company")).isNull();
            assertThat(claims.get("Role")).isNull();
        }
    }
}
