package com.openmc.alertmanager.service;

import com.openmc.alertmanager.rcon.RconClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for sending messages to Minecraft server via RCON
 */
@Service
@Slf4j
public class MinecraftMessageService {

    @Value("${minecraft.rcon.host:mcserver}")
    private String rconHost;

    @Value("${minecraft.rcon.port:25575}")
    private int rconPort;

    @Value("${minecraft.rcon.password:}")
    private String rconPassword;

    @Value("${minecraft.rcon.enabled:false}")
    private boolean enabled;

    /**
     * Send a message to the Minecraft server using the "say" command
     *
     * @param message The message to send to players
     */
    public void sendMessage(String message) {
        if (!enabled) {
            log.debug("Minecraft RCON is disabled, skipping message: {}", message);
            return;
        }

        if (rconPassword == null || rconPassword.isEmpty()) {
            log.warn("Minecraft RCON password is not configured, cannot send message: {}", message);
            return;
        }

        log.info("Sending message to Minecraft server via RCON: {}", message);

        try (RconClient client = new RconClient(rconHost, rconPort, rconPassword)) {
            String command = "say " + message;
            String response = client.sendCommand(command);
            log.debug("RCON response: {}", response);
            log.info("Message sent successfully to Minecraft server");
        } catch (Exception e) {
            log.error("Failed to send message to Minecraft server via RCON", e);
            // Don't throw exception - just log the error so other destinations can still be sent to
        }
    }
}
