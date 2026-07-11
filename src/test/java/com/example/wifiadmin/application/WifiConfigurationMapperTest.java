package com.example.wifiadmin.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.wifiadmin.api.WifiConfigurationPayload;
import com.example.wifiadmin.domain.EncryptionType;
import com.example.wifiadmin.domain.WifiBand;
import com.example.wifiadmin.domain.WifiConfiguration;
import org.junit.jupiter.api.Test;

class WifiConfigurationMapperTest {

    private final WifiConfigurationMapper mapper = new WifiConfigurationMapper();

    @Test
    void normalizesMissingEncryptionAndBlankPassword() {
        WifiConfiguration result = mapper.toDomain(new WifiConfigurationPayload(
                "CPE_001",
                WifiBand.BAND_2_4_GHZ,
                "Guest",
                null,
                "  "));

        assertThat(result.encryptionType()).isEqualTo(EncryptionType.OPEN);
        assertThat(result.password()).isNull();
    }

    @Test
    void mapsDomainConfigurationToRestPayload() {
        WifiConfiguration configuration = new WifiConfiguration(
                "CPE_001",
                WifiBand.BAND_5_GHZ,
                "Office",
                EncryptionType.WPA2_PSK,
                "secret");

        assertThat(mapper.toPayload(configuration)).isEqualTo(
                new WifiConfigurationPayload("CPE_001", WifiBand.BAND_5_GHZ, "Office", EncryptionType.WPA2_PSK, "secret"));
    }
}
