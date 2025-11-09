package com.openmc.alertmanager.exception;

/**
 * Exception thrown when alert sending fails
 */
public class AlertException extends Exception {
    
    public AlertException(String message) {
        super(message);
    }
    
    public AlertException(String message, Throwable cause) {
        super(message, cause);
    }
}
