package com.example.wifiadmin.configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.wifiadmin.models.domain.EncryptionType;
import com.example.wifiadmin.models.domain.WifiBand;
import com.example.wifiadmin.models.domain.WifiConfiguration;
import com.example.wifiadmin.services.wifi.WifiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "security.enabled=true",
        "security.issuer-uri=https://issuer.example",
        "security.audience=wifi-admin-api"
})
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class SecurityAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(name = "jwtDecoder")
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private WifiService wifiService;

    @BeforeEach
    void setUp() {
        when(wifiService.getConfiguration("CPE_001")).thenReturn(configuration());
        when(wifiService.updateConfiguration(any(WifiConfiguration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtDecoder.decode("invalid-token"))
                .thenThrow(new BadJwtException("Invalid token"));
    }

    @Test
    void missingTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/wifi-parameter/CPE_001"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/wifi-parameter/CPE_001")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWithoutReadScopeReturnsForbidden() throws Exception {
        mockMvc.perform(get("/wifi-parameter/CPE_001")
                        .with(jwt().authorities()))
                .andExpect(status().isForbidden());
    }

    @Test
    void readScopeAllowsGet() throws Exception {
        mockMvc.perform(get("/wifi-parameter/CPE_001")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_wifi:read"))))
                .andExpect(status().isOk());
    }

    @Test
    void readScopeDoesNotAllowPut() throws Exception {
        mockMvc.perform(put("/wifi-parameter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload())
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_wifi:read"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void writeScopeAllowsPut() throws Exception {
        mockMvc.perform(put("/wifi-parameter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload())
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_wifi:write"))))
                .andExpect(status().isOk());
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    private WifiConfiguration configuration() {
        return new WifiConfiguration(
                "CPE_001",
                WifiBand.BAND_2_4_GHZ,
                "Office",
                EncryptionType.WPA2_PSK,
                "secret");
    }

    private String validPayload() {
        return """
                {
                  "cpeId":"CPE_001",
                  "wifiBand":"BAND_2_4_GHZ",
                  "ssid":"Office",
                  "encryptionType":"WPA2_PSK",
                  "password":"secret"
                }
                """;
    }
}
