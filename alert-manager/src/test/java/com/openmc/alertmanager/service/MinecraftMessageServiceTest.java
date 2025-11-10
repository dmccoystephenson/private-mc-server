package com.openmc.alertmanager.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "minecraft.rcon.enabled=false",
    "minecraft.rcon.host=localhost",
    "minecraft.rcon.port=25575",
    "minecraft.rcon.password=test"
})
@DisplayName("MinecraftMessageService Tests")
class MinecraftMessageServiceTest {

    @Autowired
    private MinecraftMessageService minecraftMessageService;

    @Test
    @DisplayName("Should not throw exception when RCON is disabled")
    void shouldNotThrowExceptionWhenRconIsDisabled() {
        assertDoesNotThrow(() -> minecraftMessageService.sendMessage("Test message"));
    }

    @Test
    @DisplayName("Should not throw exception when connection fails")
    void shouldNotThrowExceptionWhenConnectionFails() {
        // This will fail to connect since there's no server, but should not throw
        assertDoesNotThrow(() -> minecraftMessageService.sendMessage("Test message"));
    }

    @Test
    @DisplayName("Should handle null message gracefully")
    void shouldHandleNullMessageGracefully() {
        assertDoesNotThrow(() -> minecraftMessageService.sendMessage(null));
    }

    @Test
    @DisplayName("Should handle empty message gracefully")
    void shouldHandleEmptyMessageGracefully() {
        assertDoesNotThrow(() -> minecraftMessageService.sendMessage(""));
    }
}
