package com.example.wifiadmin.platform.soap;

import com.example.wifiadmin.application.WifiPlatformClient;
import com.example.wifiadmin.domain.EncryptionType;
import com.example.wifiadmin.domain.WifiBand;
import com.example.wifiadmin.domain.WifiConfiguration;
import com.example.wifiadmin.exception.CpeNotFoundException;
import com.example.wifiadmin.exception.PlatformCommunicationException;
import com.example.wifiadmin.platform.soap.generated.GetCpeIdRequest;
import com.example.wifiadmin.platform.soap.generated.GetCpeIdResponse;
import com.example.wifiadmin.platform.soap.generated.UpdateCpeIdRequest;
import com.example.wifiadmin.platform.soap.generated.UpdateCpeIdResponse;
import com.example.wifiadmin.platform.soap.generated.WifiConfigurationType;
import com.example.wifiadmin.platform.soap.generated.WifiPlatformPortType;
import jakarta.xml.ws.WebServiceException;
import org.springframework.stereotype.Component;

@Component
public class CxfWifiPlatformClient implements WifiPlatformClient {

    private final WifiPlatformPortType port;
    private final SoapFaultClassifier faultClassifier;

    public CxfWifiPlatformClient(WifiPlatformPortType port) {
        this.port = port;
        this.faultClassifier = new SoapFaultClassifier();
    }

    @Override
    public WifiConfiguration getConfiguration(String cpeId) {
        GetCpeIdRequest request = new GetCpeIdRequest();
        request.setCpeId(cpeId);

        try {
            GetCpeIdResponse response = port.getCpeID(request);
            return fromSoap(response.getConfiguration());
        } catch (WebServiceException exception) {
            throw mapException(cpeId, exception);
        }
    }

    @Override
    public WifiConfiguration updateConfiguration(WifiConfiguration configuration) {
        UpdateCpeIdRequest request = new UpdateCpeIdRequest();
        request.setConfiguration(toSoap(configuration));

        try {
            UpdateCpeIdResponse response = port.updateCpeId(request);
            return fromSoap(response.getConfiguration());
        } catch (WebServiceException exception) {
            throw mapException(configuration.cpeId(), exception);
        }
    }

    private RuntimeException mapException(String cpeId, WebServiceException exception) {
        if (faultClassifier.isCpeNotFound(exception)) {
            return new CpeNotFoundException(cpeId);
        }
        return new PlatformCommunicationException("WiFi platform communication failed", exception);
    }

    private WifiConfigurationType toSoap(WifiConfiguration configuration) {
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

    private WifiConfiguration fromSoap(WifiConfigurationType configuration) {
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
