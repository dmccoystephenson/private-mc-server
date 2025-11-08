package com.openmc.alertmanager.service;

import com.openmc.alertmanager.exception.AlertException;
import com.openmc.alertmanager.model.Alert;
import com.openmc.alertmanager.model.AlertLevel;
import com.openmc.alertmanager.model.discord.DiscordEmbed;
import com.openmc.alertmanager.model.discord.DiscordEmbedFooter;
import com.openmc.alertmanager.model.discord.DiscordWebhookPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

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

    public DiscordAlertService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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
            DiscordWebhookPayload payload = buildDiscordPayload(alert);
            sendWebhook(payload);
            log.info("Discord alert sent successfully: {}", alert.getTitle());
        } catch (Exception e) {
            log.error("Failed to send Discord alert: {}", alert.getTitle(), e);
            throw new AlertException("Failed to send Discord alert", e);
        }
    }

    /**
     * Build the Discord webhook payload using DTOs
     *
     * @param alert The alert to convert to Discord format
     * @return Discord webhook payload DTO
     */
    private DiscordWebhookPayload buildDiscordPayload(Alert alert) {
        DiscordEmbedFooter footer = DiscordEmbedFooter.builder()
            .text("Source: " + (alert.getSource() != null ? alert.getSource() : "unknown"))
            .build();

        DiscordEmbed embed = DiscordEmbed.builder()
            .title(alert.getTitle())
            .description(alert.getMessage())
            .color(getColorForLevel(alert.getLevel()))
            .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
            .footer(footer)
            .build();

        return DiscordWebhookPayload.builder()
            .embeds(Collections.singletonList(embed))
            .build();
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
     * @param payload Discord webhook payload DTO
     */
    private void sendWebhook(DiscordWebhookPayload payload) throws AlertException {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<DiscordWebhookPayload> entity = new HttpEntity<>(payload, headers);
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
