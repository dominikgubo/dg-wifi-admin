package com.example.wifiadmin.mappers.persistence;

import com.example.wifiadmin.models.domain.WifiConfiguration;
import com.example.wifiadmin.persistence.entities.WifiConfigurationEntity;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class WifiConfigurationEntityMapper {

    public WifiConfigurationEntity toEntity(WifiConfiguration configuration) {
        return new WifiConfigurationEntity(
                configuration.cpeId(),
                configuration.wifiBand(),
                configuration.ssid(),
                configuration.encryptionType(),
                configuration.password(),
                Instant.now());
    }

    public WifiConfiguration toDomain(WifiConfigurationEntity entity) {
        return new WifiConfiguration(
                entity.getCpeId(),
                entity.getWifiBand(),
                entity.getSsid(),
                entity.getEncryptionType(),
                entity.getPassword());
    }
}
