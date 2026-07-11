package com.example.wifiadmin.api;

import com.example.wifiadmin.domain.EncryptionType;
import com.example.wifiadmin.domain.WifiBand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.example.wifiadmin.domain.validation.ValidWifiConfiguration;

@ValidWifiConfiguration
public record WifiConfigurationPayload(
        @NotBlank String cpeId,
        @NotNull WifiBand wifiBand,
        @NotBlank String ssid,
        EncryptionType encryptionType,
        String password) {
}
