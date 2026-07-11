package com.example.wifiadmin.services.synchronization;

import com.example.wifiadmin.configuration.SyncProperties;
import com.example.wifiadmin.exception.CpeNotFoundException;
import com.example.wifiadmin.exception.PlatformCommunicationException;
import com.example.wifiadmin.models.domain.WifiConfiguration;
import com.example.wifiadmin.persistence.stores.WifiConfigurationStore;
import com.example.wifiadmin.services.platform.WifiPlatformClient;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "database.enabled", havingValue = "true")
public class PlatformSynchronizer {

    private static final Logger log = LoggerFactory.getLogger(PlatformSynchronizer.class);

    private final WifiPlatformClient platformClient;
    private final WifiConfigurationStore store;
    private final SyncProperties properties;
    private final AtomicBoolean running = new AtomicBoolean();

    public PlatformSynchronizer(WifiPlatformClient platformClient,
                                WifiConfigurationStore store,
                                SyncProperties properties) {
        this.platformClient = platformClient;
        this.store = store;
        this.properties = properties;
    }

    @Scheduled(cron = "${platform.sync.cron}", zone = "${platform.sync.zone}")
    public void scheduledSynchronization() {
        synchronize();
    }

    public int synchronize() {
        if (!running.compareAndSet(false, true)) {
            log.info("Skipping WiFi synchronization because another run is active");
            return 0;
        }

        try {
            List<String> cpeIds = properties.cpeIds().stream()
                    .limit(properties.maxCpes())
                    .toList();
            int synchronizedCount = 0;
            for (String cpeId : cpeIds) {
                try {
                    WifiConfiguration configuration = platformClient.getConfiguration(cpeId);
                    store.save(configuration);
                    synchronizedCount++;
                } catch (CpeNotFoundException | PlatformCommunicationException exception) {
                    log.warn("WiFi synchronization failed for cpeId={}: {}", cpeId, exception.getMessage());
                }
            }
            return synchronizedCount;
        } finally {
            running.set(false);
        }
    }
}
