package com.example.wifiadmin.configuration;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "platform.sync")
public record SyncProperties(
        List<String> cpeIds,
        int maxCpes,
        String cron,
        String zone,
        boolean syncOnStartup) {
}
