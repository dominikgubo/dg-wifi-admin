package com.example.wifiadmin.configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security")
public record SecurityProperties(
        boolean enabled,
        String issuerUri,
        List<String> allowedOrigins) {
}
