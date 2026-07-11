package com.example.wifiadmin.persistence;

import com.example.wifiadmin.domain.EncryptionType;
import com.example.wifiadmin.domain.WifiBand;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "wifi_configuration")
public class WifiConfigurationEntity {

    @Id
    @Column(name = "cpe_id", nullable = false)
    private String cpeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "wifi_band", nullable = false, length = 32)
    private WifiBand wifiBand;

    @Column(name = "ssid", nullable = false, columnDefinition = "text")
    private String ssid;

    @Enumerated(EnumType.STRING)
    @Column(name = "encryption_type", nullable = false, length = 32)
    private EncryptionType encryptionType;

    @Column(name = "password", columnDefinition = "text")
    private String password;

    @Column(name = "last_synced_at", nullable = false)
    private Instant lastSyncedAt;

    protected WifiConfigurationEntity() {
    }

    public WifiConfigurationEntity(String cpeId, WifiBand wifiBand, String ssid,
                                   EncryptionType encryptionType, String password,
                                   Instant lastSyncedAt) {
        this.cpeId = cpeId;
        this.wifiBand = wifiBand;
        this.ssid = ssid;
        this.encryptionType = encryptionType;
        this.password = password;
        this.lastSyncedAt = lastSyncedAt;
    }

    public String getCpeId() {
        return cpeId;
    }

    public WifiBand getWifiBand() {
        return wifiBand;
    }

    public String getSsid() {
        return ssid;
    }

    public EncryptionType getEncryptionType() {
        return encryptionType;
    }

    public String getPassword() {
        return password;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }
}
