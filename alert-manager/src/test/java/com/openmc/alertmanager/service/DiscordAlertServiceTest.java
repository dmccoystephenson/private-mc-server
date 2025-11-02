package com.openmc.alertmanager.service;

import com.openmc.alertmanager.exception.AlertException;
import com.openmc.alertmanager.model.Alert;
import com.openmc.alertmanager.model.AlertLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "discord.webhook.url=",
    "discord.enabled=false"
})
@DisplayName("DiscordAlertService Tests")
class DiscordAlertServiceTest {

    @Autowired
    private DiscordAlertService discordAlertService;

    private Alert testAlert;

    @BeforeEach
    void setUp() {
        testAlert = Alert.builder()
            .title("Test Alert")
            .message("This is a test alert message")
            .level(AlertLevel.INFO)
            .source("test-module")
            .build();
    }

    @Test
    @DisplayName("Should handle disabled Discord alerts gracefully")
    void shouldHandleDisabledAlertsGracefully() {
        // Should not throw exception when disabled
        assertDoesNotThrow(() -> discordAlertService.sendAlert(testAlert));
    }

    @Test
    @DisplayName("Should handle missing webhook URL gracefully")
    void shouldHandleMissingWebhookUrlGracefully() {
        // Should not throw exception when webhook URL is not configured
        assertDoesNotThrow(() -> discordAlertService.sendAlert(testAlert));
    }

    @Test
    @DisplayName("Should create alert with all fields")
    void shouldCreateAlertWithAllFields() {
        assertNotNull(testAlert.getTitle());
        assertNotNull(testAlert.getMessage());
        assertNotNull(testAlert.getLevel());
        assertNotNull(testAlert.getSource());
        assertEquals("Test Alert", testAlert.getTitle());
        assertEquals("This is a test alert message", testAlert.getMessage());
        assertEquals(AlertLevel.INFO, testAlert.getLevel());
        assertEquals("test-module", testAlert.getSource());
    }

    @Test
    @DisplayName("Should handle different alert levels")
    void shouldHandleDifferentAlertLevels() {
        for (AlertLevel level : AlertLevel.values()) {
            Alert alert = Alert.builder()
                .title("Test " + level)
                .message("Test message")
                .level(level)
                .source("test")
                .build();
            
            assertDoesNotThrow(() -> discordAlertService.sendAlert(alert));
        }
    }
}
