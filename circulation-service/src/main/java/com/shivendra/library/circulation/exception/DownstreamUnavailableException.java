package com.shivendra.library.circulation.exception;

public class DownstreamUnavailableException extends RuntimeException {

    public DownstreamUnavailableException(String service, Throwable cause) {
        super(service + " is currently unavailable, please try again shortly", cause);
    }
}
