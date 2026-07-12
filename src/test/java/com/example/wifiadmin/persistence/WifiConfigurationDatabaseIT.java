package com.example.wifiadmin.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wifiadmin.models.domain.EncryptionType;
import com.example.wifiadmin.models.domain.WifiBand;
import com.example.wifiadmin.models.domain.WifiConfiguration;
import com.example.wifiadmin.persistence.stores.WifiConfigurationStore;
import com.example.wifiadmin.services.wifi.WifiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@ActiveProfiles("database")
class WifiConfigurationDatabaseIT {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private WifiConfigurationStore store;

    @Autowired
    private WifiService wifiService;

    @Test
    void flywayCreatesSchemaAndStoreRoundTripWorks() {
        WifiConfiguration configuration = configuration("CPE_DB_001", "Database WiFi");

        store.save(configuration);

        assertThat(store.findByCpeId(configuration.cpeId())).contains(configuration);
    }

    @Test
    void getUsesTheDatabaseMirror() {
        WifiConfiguration configuration = configuration("CPE_DB_002", "Mirrored WiFi");
        store.save(configuration);

        assertThat(wifiService.getConfiguration(configuration.cpeId())).isEqualTo(configuration);
    }

    @Test
    void missingDatabaseConfigurationIsNotFound() {
        assertThatThrownBy(() -> wifiService.getConfiguration("CPE_UNKNOWN"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("CPE_UNKNOWN");
    }

    private WifiConfiguration configuration(String cpeId, String ssid) {
        return new WifiConfiguration(
                cpeId,
                WifiBand.BAND_5_GHZ,
                ssid,
                EncryptionType.WPA2_PSK,
                "database-secret");
    }
}
