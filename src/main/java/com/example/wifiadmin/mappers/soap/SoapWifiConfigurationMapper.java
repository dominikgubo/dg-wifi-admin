package com.example.wifiadmin.mappers.soap;

import com.example.wifiadmin.models.domain.EncryptionType;
import com.example.wifiadmin.models.domain.WifiBand;
import com.example.wifiadmin.models.domain.WifiConfiguration;
import com.example.wifiadmin.platform.soap.generated.WifiConfigurationType;
import org.springframework.stereotype.Component;

@Component
public class SoapWifiConfigurationMapper {

    public WifiConfigurationType toSoap(WifiConfiguration configuration) {
        WifiConfigurationType result = new WifiConfigurationType();
        result.setCpeId(configuration.cpeId());
        result.setWifiBand(com.example.wifiadmin.platform.soap.generated.WifiBandType.fromValue(
                configuration.wifiBand().name()));
        result.setSsid(configuration.ssid());
        result.setEncryptionType(com.example.wifiadmin.platform.soap.generated.EncryptionType.fromValue(
                configuration.encryptionType().name()));
        result.setPassword(configuration.password());
        return result;
    }

    public WifiConfiguration fromSoap(WifiConfigurationType configuration) {
        EncryptionType encryptionType = configuration.getEncryptionType() == null
                ? EncryptionType.OPEN
                : EncryptionType.valueOf(configuration.getEncryptionType().value());
        String password = configuration.getPassword() == null || configuration.getPassword().isBlank()
                ? null
                : configuration.getPassword();

        return new WifiConfiguration(
                configuration.getCpeId(),
                WifiBand.valueOf(configuration.getWifiBand().value()),
                configuration.getSsid(),
                encryptionType,
                password);
    }
}
