package com.example.wifiadmin.services.wifi;

import com.example.wifiadmin.exceptions.CpeNotFoundException;
import com.example.wifiadmin.models.domain.WifiConfiguration;
import com.example.wifiadmin.persistence.stores.WifiConfigurationStore;
import com.example.wifiadmin.services.platform.WifiPlatformClient;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Service
public class WifiService {

    private final WifiPlatformClient platformClient;
    private final ObjectProvider<WifiConfigurationStore> storeProvider;

    public WifiService(WifiPlatformClient platformClient, ObjectProvider<WifiConfigurationStore> storeProvider) {
        this.platformClient = platformClient;
        this.storeProvider = storeProvider;
    }

    public WifiConfiguration getConfiguration(String cpeId) {
        WifiConfigurationStore store = storeProvider.getIfAvailable();
        if (store != null) {
            Optional<WifiConfiguration> configuration = store.findByCpeId(cpeId);
            return configuration.orElseThrow(() -> new CpeNotFoundException(cpeId));
        }
        return platformClient.getConfiguration(cpeId);
    }

    public WifiConfiguration updateConfiguration(WifiConfiguration configuration) {
        WifiConfiguration updated = platformClient.updateConfiguration(configuration);
        WifiConfigurationStore store = storeProvider.getIfAvailable();
        return store == null ? updated : store.save(updated);
    }
}
