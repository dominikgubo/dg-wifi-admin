package com.example.wifiadmin.services.wifi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.wifiadmin.exceptions.CpeNotFoundException;
import com.example.wifiadmin.models.domain.EncryptionType;
import com.example.wifiadmin.models.domain.WifiBand;
import com.example.wifiadmin.models.domain.WifiConfiguration;
import com.example.wifiadmin.persistence.stores.WifiConfigurationStore;
import com.example.wifiadmin.services.platform.WifiPlatformClient;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.ObjectProvider;

class WifiServiceTest {

    @Test
    void updateCallsPlatformBeforeSavingReturnedConfiguration() {
        WifiPlatformClient platformClient = mock(WifiPlatformClient.class);
        WifiConfigurationStore store = mock(WifiConfigurationStore.class);
        ObjectProvider<WifiConfigurationStore> storeProvider = mock(ObjectProvider.class);
        WifiService service = new WifiService(platformClient, storeProvider);
        WifiConfiguration requested = configuration("requested");
        WifiConfiguration returned = configuration("returned");

        when(storeProvider.getIfAvailable()).thenReturn(store);
        when(platformClient.updateConfiguration(requested)).thenReturn(returned);
        when(store.save(returned)).thenReturn(returned);

        assertThat(service.updateConfiguration(requested)).isEqualTo(returned);

        InOrder order = inOrder(platformClient, store);
        order.verify(platformClient).updateConfiguration(requested);
        order.verify(store).save(returned);
    }

    @Test
    void getUsesMirrorWhenTheDatabaseStoreIsAvailable() {
        WifiPlatformClient platformClient = mock(WifiPlatformClient.class);
        WifiConfigurationStore store = mock(WifiConfigurationStore.class);
        ObjectProvider<WifiConfigurationStore> storeProvider = mock(ObjectProvider.class);
        WifiService service = new WifiService(platformClient, storeProvider);
        WifiConfiguration configuration = configuration("mirror");

        when(storeProvider.getIfAvailable()).thenReturn(store);
        when(store.findByCpeId(configuration.cpeId())).thenReturn(Optional.of(configuration));

        assertThat(service.getConfiguration(configuration.cpeId())).isEqualTo(configuration);

        verifyNoInteractions(platformClient);
    }

    @Test
    void getReturnsNotFoundWithoutCallingPlatformOnMirrorMiss() {
        WifiPlatformClient platformClient = mock(WifiPlatformClient.class);
        WifiConfigurationStore store = mock(WifiConfigurationStore.class);
        ObjectProvider<WifiConfigurationStore> storeProvider = mock(ObjectProvider.class);
        WifiService service = new WifiService(platformClient, storeProvider);
        String cpeId = "router-with-a-nonstandard-id";

        when(storeProvider.getIfAvailable()).thenReturn(store);
        when(store.findByCpeId(cpeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getConfiguration(cpeId))
                .isInstanceOf(CpeNotFoundException.class);

        verifyNoInteractions(platformClient);
    }

    private WifiConfiguration configuration(String ssid) {
        return new WifiConfiguration(
                "CPE_SERVICE_001",
                WifiBand.BAND_2_4_GHZ,
                ssid,
                EncryptionType.WPA2_PSK,
                "service-secret");
    }
}
