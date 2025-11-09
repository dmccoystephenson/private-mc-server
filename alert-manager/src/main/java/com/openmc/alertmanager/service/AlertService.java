package com.openmc.alertmanager.service;

import com.openmc.alertmanager.exception.AlertException;
import com.openmc.alertmanager.model.Alert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Main alert service that coordinates sending alerts to various destinations
 */
@Service
@Slf4j
public class AlertService {

    private final DiscordAlertService discordAlertService;

    public AlertService(DiscordAlertService discordAlertService) {
        this.discordAlertService = discordAlertService;
    }

    /**
     * Send an alert to all configured destinations
     *
     * @param alert The alert to send
     */
    public void sendAlert(Alert alert) {
        log.info("Processing alert: {} from source: {}", alert.getTitle(), alert.getSource());
        
        // Send to Discord
        try {
            discordAlertService.sendAlert(alert);
        } catch (Exception e) {
            log.error("Failed to send alert to Discord", e);
            // Continue to other destinations even if one fails
        }
        
        // Future: Add other alert destinations here (Slack, Email, SMS, etc.)
    }
}
