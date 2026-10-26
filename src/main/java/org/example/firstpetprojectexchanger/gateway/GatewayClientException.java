package org.example.firstpetprojectexchanger.gateway;

public class GatewayClientException extends RuntimeException {

    public GatewayClientException(String message) {
        super(message);
    }

    public GatewayClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
