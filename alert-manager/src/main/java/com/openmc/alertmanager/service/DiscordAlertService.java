package com.openmc.alertmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openmc.alertmanager.exception.AlertException;
import com.openmc.alertmanager.model.Alert;
import com.openmc.alertmanager.model.AlertLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for sending alerts to Discord via webhooks
 */
@Service
@Slf4j
public class DiscordAlertService {

    // Discord embed colors
    private static final int DISCORD_COLOR_BLUE = 3447003;      // Blue for INFO
    private static final int DISCORD_COLOR_YELLOW = 16776960;  // Yellow for WARNING
    private static final int DISCORD_COLOR_RED = 15158332;     // Red for ERROR
    private static final int DISCORD_COLOR_DARK_RED = 10038562; // Dark Red for CRITICAL

    @Value("${discord.webhook.url:}")
    private String webhookUrl;

    @Value("${discord.enabled:false}")
    private boolean enabled;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public DiscordAlertService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Send an alert to Discord
     *
     * @param alert The alert to send
     * @throws AlertException if sending fails
     */
    public void sendAlert(Alert alert) throws AlertException {
        if (!enabled) {
            log.debug("Discord alerts are disabled, skipping alert: {}", alert.getTitle());
            return;
        }

        if (webhookUrl == null || webhookUrl.isEmpty()) {
            log.warn("Discord webhook URL is not configured, cannot send alert: {}", alert.getTitle());
            return;
        }

        log.info("Sending Discord alert: {} (level: {}, source: {})", 
                 alert.getTitle(), alert.getLevel(), alert.getSource());

        try {
            String payload = buildDiscordPayload(alert);
            sendWebhook(payload);
            log.info("Discord alert sent successfully: {}", alert.getTitle());
        } catch (Exception e) {
            log.error("Failed to send Discord alert: {}", alert.getTitle(), e);
            throw new AlertException("Failed to send Discord alert", e);
        }
    }

    /**
     * Build the Discord webhook payload in JSON format
     *
     * @param alert The alert to convert to Discord format
     * @return JSON payload string
     */
    private String buildDiscordPayload(Alert alert) throws AlertException {
        try {
            Map<String, Object> embed = new HashMap<>();
            embed.put("title", alert.getTitle());
            embed.put("description", alert.getMessage());
            embed.put("color", getColorForLevel(alert.getLevel()));
            embed.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            
            Map<String, Object> footer = new HashMap<>();
            footer.put("text", "Source: " + (alert.getSource() != null ? alert.getSource() : "unknown"));
            embed.put("footer", footer);

            Map<String, Object> payload = new HashMap<>();
            payload.put("embeds", new Object[]{embed});

            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new AlertException("Failed to serialize Discord payload", e);
        }
    }

    /**
     * Get Discord embed color for alert level
     *
     * @param level The alert level
     * @return Color code as integer
     */
    private int getColorForLevel(AlertLevel level) {
        return switch (level) {
            case INFO -> DISCORD_COLOR_BLUE;
            case WARNING -> DISCORD_COLOR_YELLOW;
            case ERROR -> DISCORD_COLOR_RED;
            case CRITICAL -> DISCORD_COLOR_DARK_RED;
        };
    }

    /**
     * Send webhook request to Discord
     *
     * @param payload JSON payload
     */
    private void sendWebhook(String payload) throws AlertException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                webhookUrl,
                HttpMethod.POST,
                entity,
                String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new AlertException("Discord webhook returned non-success status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new AlertException("Failed to send Discord webhook", e);
        }
    }
}
