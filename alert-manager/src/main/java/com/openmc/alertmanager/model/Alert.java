package com.openmc.alertmanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
