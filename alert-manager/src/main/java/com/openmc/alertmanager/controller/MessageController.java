package com.openmc.alertmanager.controller;

import com.openmc.alertmanager.model.Message;
import com.openmc.alertmanager.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for sending messages to various destinations
 */
@RestController
@RequestMapping("/api/messages")
@Slf4j
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Endpoint for sending messages to one or more destinations
     *
     * @param message The message to send with destinations
     * @return Response indicating success or failure
     */
    @PostMapping
    public ResponseEntity<String> sendMessage(@RequestBody Message message) {
        log.info("Received message via API to destinations: {}", message.getDestinations());
        
        try {
            messageService.sendMessage(message);
            return ResponseEntity.ok("Message sent successfully");
        } catch (Exception e) {
            log.error("Failed to send message", e);
            return ResponseEntity.internalServerError().body("Failed to send message");
        }
    }
}
