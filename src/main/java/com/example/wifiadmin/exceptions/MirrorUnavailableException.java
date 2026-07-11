package com.example.wifiadmin.exceptions;

public class MirrorUnavailableException extends RuntimeException {

    public MirrorUnavailableException(Throwable cause) {
        super("WiFi database mirror is unavailable", cause);
    }
}
