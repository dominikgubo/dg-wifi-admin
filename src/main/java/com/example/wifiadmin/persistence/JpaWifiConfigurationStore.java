package com.example.wifiadmin.persistence;

import com.example.wifiadmin.domain.WifiConfiguration;
import com.example.wifiadmin.exception.MirrorUnavailableException;
import java.time.Instant;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "database.enabled", havingValue = "true")
public class JpaWifiConfigurationStore implements WifiConfigurationStore {

    private final WifiConfigurationRepository repository;

    public JpaWifiConfigurationStore(WifiConfigurationRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WifiConfiguration> findByCpeId(String cpeId) {
        try {
            return repository.findById(cpeId).map(this::toDomain);
        } catch (DataAccessException exception) {
            throw new MirrorUnavailableException(exception);
        }
    }

    @Override
    @Transactional
    public WifiConfiguration save(WifiConfiguration configuration) {
        try {
            WifiConfigurationEntity entity = new WifiConfigurationEntity(
                    configuration.cpeId(),
                    configuration.wifiBand(),
                    configuration.ssid(),
                    configuration.encryptionType(),
                    configuration.password(),
                    Instant.now());
            return toDomain(repository.save(entity));
        } catch (DataAccessException exception) {
            throw new MirrorUnavailableException(exception);
        }
    }

    private WifiConfiguration toDomain(WifiConfigurationEntity entity) {
        return new WifiConfiguration(
                entity.getCpeId(),
                entity.getWifiBand(),
                entity.getSsid(),
                entity.getEncryptionType(),
                entity.getPassword());
    }
}
