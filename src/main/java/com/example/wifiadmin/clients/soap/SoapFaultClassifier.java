package com.example.wifiadmin.clients.soap;

import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.soap.SOAPFaultException;
import java.util.Locale;

public class SoapFaultClassifier {

    public boolean isCpeNotFound(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SOAPFaultException faultException) {
                SOAPFault fault = faultException.getFault();
                if (fault != null
                        && (containsNotFound(fault.getFaultCode())
                        || containsNotFound(fault.getFaultString()))) {
                    return true;
                }
            }
            if (containsNotFound(current.getMessage())) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean containsNotFound(String value) {
        return value != null
                && value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "")
                .contains("notfound");
    }
}
