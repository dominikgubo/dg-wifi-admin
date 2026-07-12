package com.example.wifiadmin.clients.soap;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.wifiadmin.configuration.PlatformProperties;
import com.example.wifiadmin.configuration.SoapClientConfiguration;
import com.example.wifiadmin.mappers.soap.SoapWifiConfigurationMapper;
import com.example.wifiadmin.models.domain.EncryptionType;
import com.example.wifiadmin.models.domain.WifiBand;
import com.example.wifiadmin.models.domain.WifiConfiguration;
import com.example.wifiadmin.services.platform.WifiPlatformClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CxfWifiPlatformClientIntegrationTest {

    private static MockWebServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void stopServer() throws IOException {
        server.shutdown();
    }

    @Test
    void sendsGetRequestWithSoapActionAndMapsResponse() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/xml; charset=utf-8")
                .setBody(getResponse()));

        WifiPlatformClient client = client();
        WifiConfiguration configuration = client.getConfiguration("CPE_001");
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);

        assertThat(configuration).isEqualTo(new WifiConfiguration(
                "CPE_001",
                WifiBand.BAND_2_4_GHZ,
                "Office-2G",
                EncryptionType.WPA2_PSK,
                "secret"));
        assertThat(request).isNotNull();
        assertThat(request.getHeader("SOAPAction")).contains("#getCpeID");
        assertThat(request.getBody().readUtf8())
                .contains("GetCpeIdRequest")
                .contains("tns:GetCpeIdRequest")
                .contains("tns:cpeId")
                .contains("cpeId")
                .contains("CPE_001");
    }

    @Test
    void sendsUpdateRequestWithSoapActionAndMapsResponse() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "text/xml; charset=utf-8")
                .setBody("\n" + updateResponse()));

        WifiPlatformClient client = client();
        WifiConfiguration configuration = client.updateConfiguration(new WifiConfiguration(
                "CPE_001",
                WifiBand.BAND_5_GHZ,
                "Office-5G-New",
                EncryptionType.WPA3_SAE,
                "new-secret"));
        RecordedRequest request = server.takeRequest(5, TimeUnit.SECONDS);

        assertThat(configuration.ssid()).isEqualTo("Office-5G-New");
        assertThat(request).isNotNull();
        assertThat(request.getHeader("SOAPAction")).contains("#updateCpeId");
        assertThat(request.getBody().readUtf8())
                .contains("UpdateCpeIdRequest")
                .contains("tns:UpdateCpeIdRequest")
                .contains("tns:configuration")
                .contains("tns:cpeId")
                .contains("tns:ssid")
                .contains("Office-5G-New")
                .contains("new-secret");
    }

    private WifiPlatformClient client() {
        PlatformProperties properties = new PlatformProperties(
                server.url("/platform").toString(),
                Duration.ofSeconds(2),
                Duration.ofSeconds(5));
        return new CxfWifiPlatformClient(
                new SoapClientConfiguration().wifiPlatformPort(properties),
                new SoapWifiConfigurationMapper());
    }

    private String getResponse() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://wifi-admin.local/platform/v1">
                  <soap:Body>
                    <tns:GetCpeIdResponse>
                      <tns:configuration>
                        <tns:cpeId>CPE_001</tns:cpeId>
                        <tns:wifiBand>BAND_2_4_GHZ</tns:wifiBand>
                        <tns:ssid>Office-2G</tns:ssid>
                        <tns:encryptionType>WPA2_PSK</tns:encryptionType>
                        <tns:password>secret</tns:password>
                      </tns:configuration>
                    </tns:GetCpeIdResponse>
                  </soap:Body>
                </soap:Envelope>
                """;
    }

    private String updateResponse() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tns="http://wifi-admin.local/platform/v1">
                  <soap:Body>
                    <tns:UpdateCpeIdResponse>
                      <tns:configuration>
                        <tns:cpeId>CPE_001</tns:cpeId>
                        <tns:wifiBand>BAND_5_GHZ</tns:wifiBand>
                        <tns:ssid>Office-5G-New</tns:ssid>
                        <tns:encryptionType>WPA3_SAE</tns:encryptionType>
                        <tns:password>new-secret</tns:password>
                      </tns:configuration>
                    </tns:UpdateCpeIdResponse>
                  </soap:Body>
                </soap:Envelope>
                """;
    }
}
