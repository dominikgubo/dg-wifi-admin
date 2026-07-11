package com.example.wifiadmin.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.wifiadmin.exceptions.MirrorUnavailableException;
import org.junit.jupiter.api.Test;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    void mapsMirrorFailureToServiceUnavailable() {
        var response = handler.handleMirrorFailure(new MirrorUnavailableException(new IllegalStateException()));

        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody().code()).isEqualTo("MIRROR_UNAVAILABLE");
    }
}
