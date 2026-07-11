package com.example.wifiadmin.models.domain;

public record WifiConfiguration(
        String cpeId,
        WifiBand wifiBand,
        String ssid,
        EncryptionType encryptionType,
        String password) {
}
