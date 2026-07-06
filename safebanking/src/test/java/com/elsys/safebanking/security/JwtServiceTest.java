package com.elsys.safebanking.security;

import com.elsys.safebanking.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link JwtService}.
 *
 * No Spring context is loaded — JwtService only depends on a secret string
 * and an expiration value, so it is constructed directly.
 */
class JwtServiceTest {

    // Fixed 32-char secret that satisfies the 256-bit HMAC-SHA256 minimum.
    private static final String SECRET      = "test-secret-that-is-32-chars!!xx";
    private static final long   EXPIRY_MS   = 3_600_000L; // 1 hour

    private JwtService jwtService;
    private User       alice;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRY_MS);
        alice = new User("alice@example.com", "hashed-password", "ROLE_USER");
    }

    // -------------------------------------------------------------------------
    // Token generation
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("generateToken produces a non-null, non-empty compact JWT")
    void generateToken_isNotBlank() {
        String token = jwtService.generateToken(alice);
        assertThat(token).isNotBlank();
        // JWT structure: three Base64Url-encoded segments separated by '.'
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("generateToken with extra claims includes those claims in the token")
    void generateToken_withExtraClaims_includesThem() {
        Map<String, Object> extra = Map.of("customClaim", "customValue");
        String token = jwtService.generateToken(extra, alice);

        String customClaim = jwtService.extractClaim(token,
                claims -> claims.get("customClaim", String.class));
        assertThat(customClaim).isEqualTo("customValue");
    }

    @Test
    @DisplayName("Subject claim equals the user's username")
    void generateToken_subjectMatchesUsername() {
        String token = jwtService.generateToken(alice);
        assertThat(jwtService.extractUsername(token)).isEqualTo(alice.getUsername());
    }

    @Test
    @DisplayName("issuedAt is set and not in the future")
    void generateToken_issuedAtIsPresent() {
        // JWT iat/exp are stored with second-level precision, so we truncate
        // the surrounding wall-clock timestamps to the same granularity before
        // comparing to avoid spurious failures from sub-second differences.
        long beforeSec = System.currentTimeMillis() / 1000;
        String token = jwtService.generateToken(alice);
        long afterSec  = System.currentTimeMillis() / 1000;

        long issuedAtSec = jwtService.extractClaim(token, Claims::getIssuedAt).getTime() / 1000;
        assertThat(issuedAtSec)
                .isGreaterThanOrEqualTo(beforeSec)
                .isLessThanOrEqualTo(afterSec);
    }

    @Test
    @DisplayName("Expiration is approximately issuedAt + configured expiry")
    void generateToken_expirationMatchesConfig() {
        String token     = jwtService.generateToken(alice);
        long   issuedAt  = jwtService.extractClaim(token, Claims::getIssuedAt).getTime();
        long   expiresAt = jwtService.extractClaim(token, Claims::getExpiration).getTime();

        long tolerance = 500L; // 500 ms clock-skew tolerance
        assertThat(expiresAt - issuedAt)
                .isBetween(EXPIRY_MS - tolerance, EXPIRY_MS + tolerance);
    }

    @Test
    @DisplayName("Two tokens generated for the same user at different times are not equal")
    void generateToken_twoTokensForSameUserDiffer() throws InterruptedException {
        String first  = jwtService.generateToken(alice);
        // JWT iat precision is 1 second, so sleep for 1 100 ms to guarantee a
        // different issuedAt value and therefore a different signature.
        Thread.sleep(1_100);
        String second = jwtService.generateToken(alice);
        assertThat(first).isNotEqualTo(second);
    }

    // -------------------------------------------------------------------------
    // Token validation — happy paths
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Valid token is accepted for the correct user")
    void isTokenValid_validTokenAndMatchingUser_returnsTrue() {
        String token = jwtService.generateToken(alice);
        assertThat(jwtService.isTokenValid(token, alice)).isTrue();
    }

    @Test
    @DisplayName("Token is rejected for a different user")
    void isTokenValid_tokenForDifferentUser_returnsFalse() {
        User bob = new User("bob@example.com", "hashed-pw-bob", "ROLE_USER");
        String tokenForAlice = jwtService.generateToken(alice);
        assertThat(jwtService.isTokenValid(tokenForAlice, bob)).isFalse();
    }

    // -------------------------------------------------------------------------
    // Token validation — malformed / tampered tokens
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Completely random string is rejected")
    void isTokenValid_randomString_returnsFalse() {
        assertThat(jwtService.isTokenValid("not.a.jwt", alice)).isFalse();
    }

    @Test
    @DisplayName("Token signed with a different secret is rejected")
    void isTokenValid_wrongSigningKey_returnsFalse() {
        JwtService other  = new JwtService("different-secret-32-chars-padxxx", EXPIRY_MS);
        String     foreign = other.generateToken(alice);
        assertThat(jwtService.isTokenValid(foreign, alice)).isFalse();
    }

    @Test
    @DisplayName("Tampered payload (changed subject) is rejected")
    void isTokenValid_tamperedPayload_returnsFalse() {
        String token  = jwtService.generateToken(alice);
        String[] parts = token.split("\\.");

        // Replace the payload with a Base64-encoded fake one — signature mismatch.
        String fakePayload = java.util.Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"hacker@evil.com\"}".getBytes());

        String tampered = parts[0] + "." + fakePayload + "." + parts[2];
        assertThat(jwtService.isTokenValid(tampered, alice)).isFalse();
    }

    @Test
    @DisplayName("Token with one segment missing is rejected")
    void isTokenValid_missingSegment_returnsFalse() {
        String token = jwtService.generateToken(alice);
        String[] parts = token.split("\\.");
        String truncated = parts[0] + "." + parts[1]; // no signature
        assertThat(jwtService.isTokenValid(truncated, alice)).isFalse();
    }

    @Test
    @DisplayName("Empty string token is rejected")
    void isTokenValid_emptyString_returnsFalse() {
        assertThat(jwtService.isTokenValid("", alice)).isFalse();
    }

    // -------------------------------------------------------------------------
    // Expired tokens
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Token with zero expiry is immediately expired and rejected")
    void isTokenValid_expiredToken_returnsFalse() {
        JwtService shortLived = new JwtService(SECRET, 0L); // expires instantly
        String token = shortLived.generateToken(alice);
        assertThat(jwtService.isTokenValid(token, alice)).isFalse();
    }

    // -------------------------------------------------------------------------
    // extractUsername
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("extractUsername returns the correct subject from a valid token")
    void extractUsername_validToken_returnsSubject() {
        String token = jwtService.generateToken(alice);
        assertThat(jwtService.extractUsername(token)).isEqualTo("alice@example.com");
    }
}
