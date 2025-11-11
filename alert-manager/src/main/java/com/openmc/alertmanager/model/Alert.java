package com.openmc.alertmanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents an alert to be sent to administrators or community members
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    
    /**
     * The title or subject of the alert
     */
    private String title;
    
    /**
     * The detailed message content of the alert
     */
    private String message;
    
    /**
     * The severity level of the alert
     */
    private AlertLevel level;
    
    /**
     * The source module that generated the alert (e.g., "backup-manager", "webapp")
     */
    private String source;
    
    /**
     * List of destinations where the alert should be sent.
     * If null or empty, defaults to all configured destinations.
     */
    private List<AlertDestination> destinations;
}
