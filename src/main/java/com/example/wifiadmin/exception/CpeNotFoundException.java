package com.example.wifiadmin.exception;

public class CpeNotFoundException extends RuntimeException {

    public CpeNotFoundException(String cpeId) {
        super("CPE not found: " + cpeId);
    }
}
