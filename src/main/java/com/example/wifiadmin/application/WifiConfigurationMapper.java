package com.example.wifiadmin.application;

import com.example.wifiadmin.api.WifiConfigurationPayload;
import com.example.wifiadmin.domain.EncryptionType;
import com.example.wifiadmin.domain.WifiConfiguration;
import org.springframework.stereotype.Component;

@Component
public class WifiConfigurationMapper {

    public WifiConfiguration toDomain(WifiConfigurationPayload payload) {
        EncryptionType encryptionType = payload.encryptionType() == null
                ? EncryptionType.OPEN
                : payload.encryptionType();
        String password = payload.password() == null || payload.password().isBlank()
                ? null
                : payload.password();

        return new WifiConfiguration(
                payload.cpeId(),
                payload.wifiBand(),
                payload.ssid(),
                encryptionType,
                password);
    }

    public WifiConfigurationPayload toPayload(WifiConfiguration configuration) {
        return new WifiConfigurationPayload(
                configuration.cpeId(),
                configuration.wifiBand(),
                configuration.ssid(),
                configuration.encryptionType(),
                configuration.password());
    }
}
