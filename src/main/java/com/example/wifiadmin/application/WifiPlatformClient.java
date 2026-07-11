package com.example.wifiadmin.application;

import com.example.wifiadmin.domain.WifiConfiguration;

public interface WifiPlatformClient {

    WifiConfiguration getConfiguration(String cpeId);

    WifiConfiguration updateConfiguration(WifiConfiguration configuration);
}
