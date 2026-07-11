package com.example.wifiadmin.exception;

public class MirrorUnavailableException extends RuntimeException {

    public MirrorUnavailableException(Throwable cause) {
        super("WiFi database mirror is unavailable", cause);
    }
}
