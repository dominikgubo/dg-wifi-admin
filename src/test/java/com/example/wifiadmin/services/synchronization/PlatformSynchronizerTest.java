package com.example.wifiadmin.services.synchronization;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.wifiadmin.configuration.SyncProperties;
import com.example.wifiadmin.models.domain.EncryptionType;
import com.example.wifiadmin.models.domain.WifiBand;
import com.example.wifiadmin.models.domain.WifiConfiguration;
import com.example.wifiadmin.exceptions.CpeNotFoundException;
import com.example.wifiadmin.persistence.stores.WifiConfigurationStore;
import com.example.wifiadmin.services.platform.WifiPlatformClient;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PlatformSynchronizerTest {

    @Test
    void synchronizesOnlyConfiguredMaximumAndContinuesAfterFailure() {
        WifiPlatformClient platformClient = Mockito.mock(WifiPlatformClient.class);
        WifiConfigurationStore store = Mockito.mock(WifiConfigurationStore.class);
        SyncProperties properties = new SyncProperties(
                List.of("CPE_001", "CPE_002", "CPE_003"),
                2,
                "0 0 2 * * *",
                "Europe/Zagreb",
                false);
        WifiConfiguration configuration = new WifiConfiguration(
                "CPE_001", WifiBand.BAND_2_4_GHZ, "Office", EncryptionType.WPA2_PSK, "secret");
        when(platformClient.getConfiguration("CPE_001")).thenReturn(configuration);
        when(platformClient.getConfiguration("CPE_002"))
                .thenThrow(new CpeNotFoundException("CPE_002"));

        PlatformSynchronizer synchronizer = new PlatformSynchronizer(platformClient, store, properties);

        org.assertj.core.api.Assertions.assertThat(synchronizer.synchronize()).isEqualTo(1);
        verify(store).save(configuration);
        verify(platformClient).getConfiguration("CPE_001");
        verify(platformClient).getConfiguration("CPE_002");
        verify(platformClient, never()).getConfiguration("CPE_003");
    }
}
