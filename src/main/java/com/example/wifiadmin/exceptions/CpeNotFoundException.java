package com.example.wifiadmin.exceptions;

public class CpeNotFoundException extends RuntimeException {

    public CpeNotFoundException(String cpeId) {
        super("CPE not found: " + cpeId);
    }
}
