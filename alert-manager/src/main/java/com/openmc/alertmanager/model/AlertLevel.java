package com.openmc.alertmanager.model;

/**
 * Severity levels for alerts
 */
public enum AlertLevel {
    INFO,       // Informational messages
    WARNING,    // Warning messages that require attention
    ERROR,      // Error messages for failures
    CRITICAL    // Critical issues requiring immediate attention
}
