package com.example.wifiadmin.domain;

public record WifiConfiguration(
        String cpeId,
        WifiBand wifiBand,
        String ssid,
        EncryptionType encryptionType,
        String password) {
}
