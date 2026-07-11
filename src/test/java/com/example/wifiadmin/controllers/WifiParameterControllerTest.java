package com.example.wifiadmin.controllers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.wifiadmin.mappers.api.WifiConfigurationMapper;
import com.example.wifiadmin.models.domain.EncryptionType;
import com.example.wifiadmin.models.domain.WifiBand;
import com.example.wifiadmin.models.domain.WifiConfiguration;
import com.example.wifiadmin.exceptions.CpeNotFoundException;
import com.example.wifiadmin.exceptions.PlatformCommunicationException;
import com.example.wifiadmin.services.wifi.WifiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class WifiParameterControllerTest {

    private WifiService wifiService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        wifiService = mock(WifiService.class);
        WifiConfigurationMapper mapper = new WifiConfigurationMapper();
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new WifiParameterController(wifiService, mapper))
                .setControllerAdvice(new RestExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void getReturnsConfiguration() throws Exception {
        when(wifiService.getConfiguration("CPE_001")).thenReturn(configuration());

        mockMvc.perform(get("/wifi-parameter/CPE_001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpeId").value("CPE_001"))
                .andExpect(jsonPath("$.wifiBand").value("BAND_2_4_GHZ"))
                .andExpect(jsonPath("$.encryptionType").value("WPA2_PSK"));
    }

    @Test
    void putRejectsPasswordlessSecureConfiguration() throws Exception {
        mockMvc.perform(put("/wifi-parameter")
                        .contentType("application/json")
                        .content("""
                                {
                                  "cpeId":"CPE_001",
                                  "wifiBand":"BAND_2_4_GHZ",
                                  "ssid":"Office",
                                  "encryptionType":"WPA2_PSK"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void getMapsUnknownCpeToNotFound() throws Exception {
        when(wifiService.getConfiguration("CPE_UNKNOWN"))
                .thenThrow(new CpeNotFoundException("CPE_UNKNOWN"));

        mockMvc.perform(get("/wifi-parameter/CPE_UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CPE_NOT_FOUND"));
    }

    @Test
    void getMapsPlatformFailureToBadGateway() throws Exception {
        when(wifiService.getConfiguration("CPE_001"))
                .thenThrow(new PlatformCommunicationException("failure", new RuntimeException()));

        mockMvc.perform(get("/wifi-parameter/CPE_001"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("PLATFORM_ERROR"));
    }

    private WifiConfiguration configuration() {
        return new WifiConfiguration(
                "CPE_001",
                WifiBand.BAND_2_4_GHZ,
                "Office",
                EncryptionType.WPA2_PSK,
                "secret");
    }
}
