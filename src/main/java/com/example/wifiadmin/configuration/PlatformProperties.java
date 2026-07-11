package com.example.wifiadmin.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "platform")
public record PlatformProperties(
        String url,
        Duration connectTimeout,
        Duration readTimeout) {
}
