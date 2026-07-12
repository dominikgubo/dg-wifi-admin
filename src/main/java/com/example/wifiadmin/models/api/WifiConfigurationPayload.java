package com.example.wifiadmin.models.api;

import com.example.wifiadmin.models.domain.EncryptionType;
import com.example.wifiadmin.models.domain.WifiBand;
import com.example.wifiadmin.validators.ValidWifiConfiguration;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ValidWifiConfiguration
public record WifiConfigurationPayload(
        @NotBlank String cpeId,
        @NotNull WifiBand wifiBand,
        @NotBlank String ssid,
        EncryptionType encryptionType,
        String password) {
}
