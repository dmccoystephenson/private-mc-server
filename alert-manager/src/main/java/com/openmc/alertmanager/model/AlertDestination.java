package com.openmc.alertmanager.model;

/**
 * Enum representing possible destinations for alerts
 */
public enum AlertDestination {
    /**
     * Send alert to Discord
     */
    DISCORD,
    
    /**
     * Send alert to Minecraft server
     */
    MINECRAFT
}
