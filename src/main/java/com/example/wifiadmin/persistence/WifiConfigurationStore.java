package com.example.wifiadmin.persistence;

import com.example.wifiadmin.domain.WifiConfiguration;
import java.util.Optional;

public interface WifiConfigurationStore {

    Optional<WifiConfiguration> findByCpeId(String cpeId);

    WifiConfiguration save(WifiConfiguration configuration);
}
