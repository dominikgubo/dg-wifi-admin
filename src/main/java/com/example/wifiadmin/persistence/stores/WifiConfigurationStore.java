package com.example.wifiadmin.persistence.stores;

import com.example.wifiadmin.models.domain.WifiConfiguration;
import java.util.Optional;

public interface WifiConfigurationStore {

    Optional<WifiConfiguration> findByCpeId(String cpeId);

    WifiConfiguration save(WifiConfiguration configuration);
}
