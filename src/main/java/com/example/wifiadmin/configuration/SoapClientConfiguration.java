package com.example.wifiadmin.configuration;

import com.example.wifiadmin.platform.soap.generated.WifiPlatformPortType;
import com.example.wifiadmin.platform.soap.generated.WifiPlatformService;
import jakarta.xml.ws.BindingProvider;
import java.util.Map;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SoapClientConfiguration {

    @Bean
    public WifiPlatformPortType wifiPlatformPort(PlatformProperties properties) {
        WifiPlatformService service = new WifiPlatformService();
        WifiPlatformPortType port = service.getWifiPlatformPort();

        BindingProvider bindingProvider = (BindingProvider) port;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, properties.url());

        Client client = ClientProxy.getClient(port);
        HTTPConduit conduit = (HTTPConduit) client.getConduit();
        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setConnectionTimeout(properties.connectTimeout().toMillis());
        policy.setReceiveTimeout(properties.readTimeout().toMillis());
        conduit.setClient(policy);

        return port;
    }
}
