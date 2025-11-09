package com.openmc.alertmanager.service;

import com.openmc.alertmanager.model.Alert;
import com.openmc.alertmanager.model.AlertLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("AlertService Tests")
class AlertServiceTest {

    @Autowired
    private AlertService alertService;

    @MockBean
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
    @DisplayName("Should send alert to Discord service")
    void shouldSendAlertToDiscordService() throws Exception {
        alertService.sendAlert(testAlert);
        
        verify(discordAlertService, times(1)).sendAlert(testAlert);
    }

    @Test
    @DisplayName("Should not throw exception when Discord service fails")
    void shouldNotThrowExceptionWhenDiscordServiceFails() throws Exception {
        doThrow(new RuntimeException("Discord error")).when(discordAlertService).sendAlert(any());
        
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
        
        verify(discordAlertService, times(AlertLevel.values().length)).sendAlert(any());
    }
}
