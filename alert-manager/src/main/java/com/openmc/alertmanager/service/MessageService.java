package com.openmc.alertmanager.service;

import com.openmc.alertmanager.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Main message service that coordinates sending messages to various destinations
 */
@Service
@Slf4j
public class MessageService {

    private final MinecraftMessageService minecraftMessageService;

    public MessageService(MinecraftMessageService minecraftMessageService) {
        this.minecraftMessageService = minecraftMessageService;
    }

    /**
     * Send a message to all specified destinations
     *
     * @param message The message to send
     */
    public void sendMessage(Message message) {
        log.info("Processing message to destinations: {}", message.getDestinations());
        
        if (message.getDestinations() == null || message.getDestinations().isEmpty()) {
            log.warn("No destinations specified for message: {}", message.getText());
            return;
        }
        
        // Send to each requested destination
        for (String destination : message.getDestinations()) {
            try {
                switch (destination.toLowerCase()) {
                    case "minecraft":
                        minecraftMessageService.sendMessage(message.getText());
                        break;
                    default:
                        log.warn("Unknown destination: {}", destination);
                }
            } catch (Exception e) {
                log.error("Failed to send message to destination: {}", destination, e);
                // Continue to other destinations even if one fails
            }
        }
    }
}
