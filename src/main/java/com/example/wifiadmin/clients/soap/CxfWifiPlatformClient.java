package com.example.wifiadmin.clients.soap;

import com.example.wifiadmin.mappers.soap.SoapWifiConfigurationMapper;
import com.example.wifiadmin.models.domain.WifiConfiguration;
import com.example.wifiadmin.exception.CpeNotFoundException;
import com.example.wifiadmin.exception.PlatformCommunicationException;
import com.example.wifiadmin.platform.soap.generated.GetCpeIdRequest;
import com.example.wifiadmin.platform.soap.generated.GetCpeIdResponse;
import com.example.wifiadmin.platform.soap.generated.UpdateCpeIdRequest;
import com.example.wifiadmin.platform.soap.generated.UpdateCpeIdResponse;
import com.example.wifiadmin.platform.soap.generated.WifiPlatformPortType;
import com.example.wifiadmin.services.platform.WifiPlatformClient;
import jakarta.xml.ws.WebServiceException;
import org.springframework.stereotype.Component;

@Component
public class CxfWifiPlatformClient implements WifiPlatformClient {

    private final WifiPlatformPortType port;
    private final SoapFaultClassifier faultClassifier;
    private final SoapWifiConfigurationMapper mapper;

    public CxfWifiPlatformClient(WifiPlatformPortType port, SoapWifiConfigurationMapper mapper) {
        this.port = port;
        this.faultClassifier = new SoapFaultClassifier();
        this.mapper = mapper;
    }

    @Override
    public WifiConfiguration getConfiguration(String cpeId) {
        GetCpeIdRequest request = new GetCpeIdRequest();
        request.setCpeId(cpeId);

        try {
            GetCpeIdResponse response = port.getCpeID(request);
            return mapper.fromSoap(response.getConfiguration());
        } catch (WebServiceException exception) {
            throw mapException(cpeId, exception);
        }
    }

    @Override
    public WifiConfiguration updateConfiguration(WifiConfiguration configuration) {
        UpdateCpeIdRequest request = new UpdateCpeIdRequest();
        request.setConfiguration(mapper.toSoap(configuration));

        try {
            UpdateCpeIdResponse response = port.updateCpeId(request);
            return mapper.fromSoap(response.getConfiguration());
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

}
