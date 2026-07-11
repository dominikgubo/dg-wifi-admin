package com.example.wifiadmin.api;

import com.example.wifiadmin.application.WifiConfigurationMapper;
import com.example.wifiadmin.application.WifiService;
import com.example.wifiadmin.domain.WifiConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/wifi-parameter")
public class WifiParameterController {

    private final WifiService wifiService;
    private final WifiConfigurationMapper mapper;

    public WifiParameterController(WifiService wifiService, WifiConfigurationMapper mapper) {
        this.wifiService = wifiService;
        this.mapper = mapper;
    }

    @GetMapping(value = "/{cpeId}", produces = "application/json")
    public WifiConfigurationPayload getWifiParameter(@PathVariable @NotBlank String cpeId) {
        WifiConfiguration configuration = wifiService.getConfiguration(cpeId);
        return mapper.toPayload(configuration);
    }

    @PutMapping(produces = "application/json", consumes = "application/json")
    public WifiConfigurationPayload putWifiParameter(
            @Valid @RequestBody WifiConfigurationPayload payload) {
        WifiConfiguration updated = wifiService.updateConfiguration(mapper.toDomain(payload));
        return mapper.toPayload(updated);
    }
}
