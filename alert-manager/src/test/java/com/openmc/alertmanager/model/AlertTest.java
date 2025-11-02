package com.openmc.alertmanager.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Alert Model Tests")
class AlertTest {

    @Test
    @DisplayName("Should create alert with builder")
    void shouldCreateAlertWithBuilder() {
        Alert alert = Alert.builder()
            .title("Test Title")
            .message("Test Message")
            .level(AlertLevel.WARNING)
            .source("test-source")
            .build();

        assertEquals("Test Title", alert.getTitle());
        assertEquals("Test Message", alert.getMessage());
        assertEquals(AlertLevel.WARNING, alert.getLevel());
        assertEquals("test-source", alert.getSource());
    }

    @Test
    @DisplayName("Should support all alert levels")
    void shouldSupportAllAlertLevels() {
        assertNotNull(AlertLevel.INFO);
        assertNotNull(AlertLevel.WARNING);
        assertNotNull(AlertLevel.ERROR);
        assertNotNull(AlertLevel.CRITICAL);
        assertEquals(4, AlertLevel.values().length);
    }

    @Test
    @DisplayName("Should allow null values in alert")
    void shouldAllowNullValuesInAlert() {
        Alert alert = Alert.builder().build();
        
        assertNull(alert.getTitle());
        assertNull(alert.getMessage());
        assertNull(alert.getLevel());
        assertNull(alert.getSource());
    }

    @Test
    @DisplayName("Should set and get all properties")
    void shouldSetAndGetAllProperties() {
        Alert alert = new Alert();
        alert.setTitle("Title");
        alert.setMessage("Message");
        alert.setLevel(AlertLevel.ERROR);
        alert.setSource("source");

        assertEquals("Title", alert.getTitle());
        assertEquals("Message", alert.getMessage());
        assertEquals(AlertLevel.ERROR, alert.getLevel());
        assertEquals("source", alert.getSource());
    }
}
