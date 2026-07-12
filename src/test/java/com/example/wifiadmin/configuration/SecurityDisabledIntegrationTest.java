package com.example.wifiadmin.configuration;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.wifiadmin.models.domain.EncryptionType;
import com.example.wifiadmin.models.domain.WifiBand;
import com.example.wifiadmin.models.domain.WifiConfiguration;
import com.example.wifiadmin.services.wifi.WifiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "security.enabled=false")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class SecurityDisabledIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WifiService wifiService;

    @Test
    void disabledSecurityAllowsUnauthenticatedRequest() throws Exception {
        when(wifiService.getConfiguration("CPE_001")).thenReturn(new WifiConfiguration(
                "CPE_001",
                WifiBand.BAND_2_4_GHZ,
                "Office",
                EncryptionType.WPA2_PSK,
                "secret"));

        mockMvc.perform(get("/wifi-parameter/CPE_001"))
                .andExpect(status().isOk());
    }
}
