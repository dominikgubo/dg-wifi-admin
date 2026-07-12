package com.example.wifiadmin.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.cors.CorsConfiguration;

class SecurityConfigurationTest {

    private final SecurityConfiguration configuration = new SecurityConfiguration();

    @Test
    void corsConfigurationKeepsOnlyNonblankOrigins() {
        SecurityProperties properties = new SecurityProperties(
                false,
                "",
                "",
                List.of("http://localhost:3000", " ", "http://localhost:3001"));

        CorsConfiguration cors = configuration.corsConfigurationSource(properties)
                .getCorsConfiguration(new MockHttpServletRequest("GET", "/wifi-parameter/CPE_001"));

        assertThat(cors).isNotNull();
        assertThat(cors.getAllowedOrigins())
                .containsExactly("http://localhost:3000", "http://localhost:3001");
        assertThat(cors.getAllowedMethods()).containsExactly("GET", "PUT", "OPTIONS");
    }

    @Test
    void enabledSecurityRequiresIssuer() {
        SecurityProperties properties = new SecurityProperties(true, " ", "wifi-admin-api", List.of());

        assertThatThrownBy(() -> configuration.jwtDecoder(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("issuer-uri");
    }

    @Test
    void enabledSecurityRequiresAudience() {
        SecurityProperties properties = new SecurityProperties(true, "https://issuer.example", " ", List.of());

        assertThatThrownBy(() -> configuration.jwtDecoder(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("audience");
    }

    @Test
    void jwtValidatorRejectsTokenForAnotherAudience() {
        SecurityProperties properties = new SecurityProperties(
                true,
                "https://issuer.example",
                "wifi-admin-api",
                List.of());
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("iss", properties.issuerUri())
                .claim("aud", List.of("another-api"))
                .issuedAt(now.minusSeconds(30))
                .expiresAt(now.plusSeconds(300))
                .build();

        assertThat(configuration.jwtValidator(properties).validate(jwt).hasErrors()).isTrue();
    }
}
