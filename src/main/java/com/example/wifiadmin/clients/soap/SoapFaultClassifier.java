package com.example.wifiadmin.clients.soap;

import jakarta.xml.ws.soap.SOAPFaultException;
import java.util.Locale;

public class SoapFaultClassifier {

    public boolean isCpeNotFound(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SOAPFaultException faultException) {
                String faultCode = faultException.getFault().getFaultCode();
                String faultString = faultException.getFault().getFaultString();
                return containsNotFound(faultCode) || containsNotFound(faultString);
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean containsNotFound(String value) {
        return value != null && value.toLowerCase(Locale.ROOT).replaceAll("\\s+", "").contains("notfound");
    }
}
