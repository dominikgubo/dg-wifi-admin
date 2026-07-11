package com.example.wifiadmin.configuration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.wifiadmin.services.synchronization.PlatformSynchronizer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;

class StartupSynchronizationTest {

    @Test
    void runsSynchronizationWhenStartupRunnerIsEnabled() {
        PlatformSynchronizer synchronizer = mock(PlatformSynchronizer.class);
        StartupSynchronization startupSynchronization = new StartupSynchronization(synchronizer);

        startupSynchronization.run(mock(ApplicationArguments.class));

        verify(synchronizer).synchronize();
    }
}
