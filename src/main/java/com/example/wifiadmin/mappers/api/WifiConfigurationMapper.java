package com.example.wifiadmin.mappers.api;

import com.example.wifiadmin.models.api.WifiConfigurationPayload;
import com.example.wifiadmin.models.domain.EncryptionType;
import com.example.wifiadmin.models.domain.WifiConfiguration;
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
