package com.example.wifiadmin.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

class SecurityConfigurationTest {

    private final SecurityConfiguration configuration = new SecurityConfiguration();

    @Test
    void corsConfigurationKeepsOnlyNonblankOrigins() {
        SecurityProperties properties = new SecurityProperties(
                false,
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
        SecurityProperties properties = new SecurityProperties(true, " ", List.of());

        assertThatThrownBy(() -> configuration.jwtDecoder(properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("issuer-uri");
    }
}
