package com.example.wifiadmin.services.platform;

import com.example.wifiadmin.models.domain.WifiConfiguration;

public interface WifiPlatformClient {

    WifiConfiguration getConfiguration(String cpeId);

    WifiConfiguration updateConfiguration(WifiConfiguration configuration);
}
