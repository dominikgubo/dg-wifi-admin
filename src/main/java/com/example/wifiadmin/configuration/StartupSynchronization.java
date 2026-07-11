package com.example.wifiadmin.configuration;

import com.example.wifiadmin.application.PlatformSynchronizer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "platform.sync.sync-on-startup", havingValue = "true")
public class StartupSynchronization implements ApplicationRunner {

    private final PlatformSynchronizer synchronizer;

    public StartupSynchronization(PlatformSynchronizer synchronizer) {
        this.synchronizer = synchronizer;
    }

    @Override
    public void run(ApplicationArguments args) {
        synchronizer.synchronize();
    }
}
