package com.example.wifiadmin.configuration;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityProperties properties)
            throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults());

        if (!properties.enabled()) {
            return http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll()).build();
        }

        return http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/wifi-parameter/**").hasAuthority("SCOPE_wifi:read")
                        .requestMatchers(HttpMethod.PUT, "/wifi-parameter").hasAuthority("SCOPE_wifi:write")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(SecurityProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> allowedOrigins = properties.allowedOrigins() == null
                ? List.of()
                : properties.allowedOrigins().stream().filter(origin -> !origin.isBlank()).toList();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "PUT", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @ConditionalOnProperty(name = "security.enabled", havingValue = "true")
    public JwtDecoder jwtDecoder(SecurityProperties properties) {
        if (properties.issuerUri() == null || properties.issuerUri().isBlank()) {
            throw new IllegalStateException("security.issuer-uri is required when security is enabled");
        }
        return JwtDecoders.fromIssuerLocation(properties.issuerUri());
    }
}
