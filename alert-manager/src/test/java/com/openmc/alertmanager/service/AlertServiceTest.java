package com.openmc.alertmanager.service;

import com.openmc.alertmanager.model.Alert;
import com.openmc.alertmanager.model.AlertDestination;
import com.openmc.alertmanager.model.AlertLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("AlertService Tests")
class AlertServiceTest {

    @Autowired
    private AlertService alertService;

    @MockBean
    private DiscordAlertService discordAlertService;

    @MockBean
    private MinecraftMessageService minecraftMessageService;

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
    @DisplayName("Should send alert to all destinations when none specified")
    void shouldSendAlertToAllDestinationsWhenNoneSpecified() throws Exception {
        alertService.sendAlert(testAlert);
        
        // Should send to both Discord and Minecraft
        verify(discordAlertService, times(1)).sendAlert(testAlert);
        verify(minecraftMessageService, times(1)).sendMessage(testAlert.getMessage());
    }

    @Test
    @DisplayName("Should send alert to Discord only when specified")
    void shouldSendAlertToDiscordOnlyWhenSpecified() throws Exception {
        testAlert.setDestinations(Collections.singletonList(AlertDestination.DISCORD));
        alertService.sendAlert(testAlert);
        
        verify(discordAlertService, times(1)).sendAlert(testAlert);
        verify(minecraftMessageService, never()).sendMessage(anyString());
    }

    @Test
    @DisplayName("Should send alert to Minecraft only when specified")
    void shouldSendAlertToMinecraftOnlyWhenSpecified() throws Exception {
        testAlert.setDestinations(Collections.singletonList(AlertDestination.MINECRAFT));
        alertService.sendAlert(testAlert);
        
        verify(discordAlertService, never()).sendAlert(any());
        verify(minecraftMessageService, times(1)).sendMessage(testAlert.getMessage());
    }

    @Test
    @DisplayName("Should not throw exception when Discord service fails")
    void shouldNotThrowExceptionWhenDiscordServiceFails() throws Exception {
        doThrow(new RuntimeException("Discord error")).when(discordAlertService).sendAlert(any());
        
        // Should handle the exception gracefully
        assertDoesNotThrow(() -> alertService.sendAlert(testAlert));
    }

    @Test
    @DisplayName("Should not throw exception when Minecraft service fails")
    void shouldNotThrowExceptionWhenMinecraftServiceFails() throws Exception {
        doThrow(new RuntimeException("Minecraft error")).when(minecraftMessageService).sendMessage(anyString());
        
        // Should handle the exception gracefully
        assertDoesNotThrow(() -> alertService.sendAlert(testAlert));
    }

    @Test
    @DisplayName("Should process alerts with different levels")
    void shouldProcessAlertsWithDifferentLevels() throws Exception {
        for (AlertLevel level : AlertLevel.values()) {
            Alert alert = Alert.builder()
                .title("Test " + level)
                .message("Test message")
                .level(level)
                .source("test")
                .build();
            
            alertService.sendAlert(alert);
        }
        
        // Each level should be sent to all destinations (Discord + Minecraft)
        verify(discordAlertService, times(AlertLevel.values().length)).sendAlert(any());
        verify(minecraftMessageService, times(AlertLevel.values().length)).sendMessage(anyString());
    }
}
