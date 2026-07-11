package com.example.wifiadmin.platform.soap;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.soap.SOAPFaultException;
import org.junit.jupiter.api.Test;

class SoapFaultClassifierTest {

    private final SoapFaultClassifier classifier = new SoapFaultClassifier();

    @Test
    void identifiesNotFoundFault() {
        SOAPFault fault = mock(SOAPFault.class);
        when(fault.getFaultCode()).thenReturn("tns:NotFound");

        assertThat(classifier.isCpeNotFound(new SOAPFaultException(fault))).isTrue();
    }

    @Test
    void doesNotIdentifyGenericFaultAsNotFound() {
        SOAPFault fault = mock(SOAPFault.class);
        when(fault.getFaultString()).thenReturn("Platform unavailable");

        assertThat(classifier.isCpeNotFound(new SOAPFaultException(fault))).isFalse();
    }
}
