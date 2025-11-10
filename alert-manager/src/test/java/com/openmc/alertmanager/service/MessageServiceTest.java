package com.openmc.alertmanager.service;

import com.openmc.alertmanager.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("MessageService Tests")
class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @MockBean
    private MinecraftMessageService minecraftMessageService;

    private Message testMessage;

    @BeforeEach
    void setUp() {
        testMessage = Message.builder()
            .text("Test message")
            .destinations(Collections.singletonList("minecraft"))
            .build();
    }

    @Test
    @DisplayName("Should send message to Minecraft destination")
    void shouldSendMessageToMinecraftDestination() {
        messageService.sendMessage(testMessage);
        
        verify(minecraftMessageService, times(1)).sendMessage("Test message");
    }

    @Test
    @DisplayName("Should send message to multiple destinations")
    void shouldSendMessageToMultipleDestinations() {
        Message multiDestMessage = Message.builder()
            .text("Multi-destination message")
            .destinations(Arrays.asList("minecraft", "minecraft"))
            .build();
        
        messageService.sendMessage(multiDestMessage);
        
        verify(minecraftMessageService, times(2)).sendMessage("Multi-destination message");
    }

    @Test
    @DisplayName("Should handle empty destinations gracefully")
    void shouldHandleEmptyDestinationsGracefully() {
        Message emptyDestMessage = Message.builder()
            .text("No destinations")
            .destinations(Collections.emptyList())
            .build();
        
        assertDoesNotThrow(() -> messageService.sendMessage(emptyDestMessage));
        verify(minecraftMessageService, never()).sendMessage(anyString());
    }

    @Test
    @DisplayName("Should handle null destinations gracefully")
    void shouldHandleNullDestinationsGracefully() {
        Message nullDestMessage = Message.builder()
            .text("Null destinations")
            .destinations(null)
            .build();
        
        assertDoesNotThrow(() -> messageService.sendMessage(nullDestMessage));
        verify(minecraftMessageService, never()).sendMessage(anyString());
    }

    @Test
    @DisplayName("Should not throw exception when destination service fails")
    void shouldNotThrowExceptionWhenDestinationServiceFails() {
        doThrow(new RuntimeException("Connection error")).when(minecraftMessageService).sendMessage(anyString());
        
        // Should handle the exception gracefully
        assertDoesNotThrow(() -> messageService.sendMessage(testMessage));
    }

    @Test
    @DisplayName("Should ignore unknown destinations")
    void shouldIgnoreUnknownDestinations() {
        Message unknownDestMessage = Message.builder()
            .text("Unknown destination test")
            .destinations(Arrays.asList("unknown-destination", "minecraft"))
            .build();
        
        messageService.sendMessage(unknownDestMessage);
        
        // Should only call Minecraft service once
        verify(minecraftMessageService, times(1)).sendMessage("Unknown destination test");
    }
}
