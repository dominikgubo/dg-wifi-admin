package com.example.wifiadmin.mappers.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.wifiadmin.models.domain.EncryptionType;
import com.example.wifiadmin.models.domain.WifiBand;
import com.example.wifiadmin.models.domain.WifiConfiguration;
import com.example.wifiadmin.persistence.entities.WifiConfigurationEntity;
import org.junit.jupiter.api.Test;

class WifiConfigurationEntityMapperTest {

    private final WifiConfigurationEntityMapper mapper = new WifiConfigurationEntityMapper();

    @Test
    void mapsConfigurationToEntityAndBack() {
        WifiConfiguration configuration = new WifiConfiguration(
                "CPE_MAPPER_001",
                WifiBand.BAND_5_GHZ,
                "Mapped WiFi",
                EncryptionType.WPA3_SAE,
                "mapper-secret");

        WifiConfigurationEntity entity = mapper.toEntity(configuration);

        assertThat(entity.getLastSyncedAt()).isNotNull();
        assertThat(mapper.toDomain(entity)).isEqualTo(configuration);
    }
}
