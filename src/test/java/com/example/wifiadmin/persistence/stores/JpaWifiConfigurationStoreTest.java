package com.example.wifiadmin.persistence.stores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.wifiadmin.exceptions.MirrorUnavailableException;
import com.example.wifiadmin.mappers.persistence.WifiConfigurationEntityMapper;
import com.example.wifiadmin.models.domain.EncryptionType;
import com.example.wifiadmin.models.domain.WifiBand;
import com.example.wifiadmin.models.domain.WifiConfiguration;
import com.example.wifiadmin.persistence.entities.WifiConfigurationEntity;
import com.example.wifiadmin.persistence.repositories.WifiConfigurationRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

class JpaWifiConfigurationStoreTest {

    private final WifiConfigurationEntityMapper mapper = new WifiConfigurationEntityMapper();

    @Test
    void findsAndSavesConfigurations() {
        WifiConfigurationRepository repository = mock(WifiConfigurationRepository.class);
        WifiConfigurationStore store = new JpaWifiConfigurationStore(repository, mapper);
        WifiConfiguration configuration = configuration();
        WifiConfigurationEntity entity = mapper.toEntity(configuration);

        when(repository.findById(configuration.cpeId())).thenReturn(Optional.of(entity));
        when(repository.save(org.mockito.ArgumentMatchers.any(WifiConfigurationEntity.class)))
                .thenReturn(entity);

        assertThat(store.findByCpeId(configuration.cpeId())).contains(configuration);
        assertThat(store.save(configuration)).isEqualTo(configuration);
    }

    @Test
    void translatesDatabaseReadFailure() {
        WifiConfigurationRepository repository = mock(WifiConfigurationRepository.class);
        WifiConfigurationStore store = new JpaWifiConfigurationStore(repository, mapper);
        when(repository.findById("CPE_FAILURE"))
                .thenThrow(new DataAccessResourceFailureException("database down"));

        assertThatThrownBy(() -> store.findByCpeId("CPE_FAILURE"))
                .isInstanceOf(MirrorUnavailableException.class);
    }

    @Test
    void translatesDatabaseWriteFailure() {
        WifiConfigurationRepository repository = mock(WifiConfigurationRepository.class);
        WifiConfigurationStore store = new JpaWifiConfigurationStore(repository, mapper);
        when(repository.save(org.mockito.ArgumentMatchers.any(WifiConfigurationEntity.class)))
                .thenThrow(new DataAccessResourceFailureException("database down"));

        assertThatThrownBy(() -> store.save(configuration()))
                .isInstanceOf(MirrorUnavailableException.class);
    }

    private WifiConfiguration configuration() {
        return new WifiConfiguration(
                "CPE_STORE_001",
                WifiBand.BAND_2_4_GHZ,
                "Store WiFi",
                EncryptionType.WPA2_PSK,
                "store-secret");
    }
}
