package com.example.wifiadmin.persistence.stores;

import com.example.wifiadmin.exceptions.MirrorUnavailableException;
import com.example.wifiadmin.mappers.persistence.WifiConfigurationEntityMapper;
import com.example.wifiadmin.models.domain.WifiConfiguration;
import com.example.wifiadmin.persistence.repositories.WifiConfigurationRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@ConditionalOnProperty(name = "database.enabled", havingValue = "true")
public class JpaWifiConfigurationStore implements WifiConfigurationStore {

    private final WifiConfigurationRepository repository;
    private final WifiConfigurationEntityMapper mapper;

    public JpaWifiConfigurationStore(WifiConfigurationRepository repository,
                                     WifiConfigurationEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WifiConfiguration> findByCpeId(String cpeId) {
        try {
            return repository.findById(cpeId).map(mapper::toDomain);
        } catch (DataAccessException exception) {
            throw new MirrorUnavailableException(exception);
        }
    }

    @Override
    @Transactional
    public WifiConfiguration save(WifiConfiguration configuration) {
        try {
            return mapper.toDomain(repository.save(mapper.toEntity(configuration)));
        } catch (DataAccessException exception) {
            throw new MirrorUnavailableException(exception);
        }
    }

}
