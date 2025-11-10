package com.openmc.alertmanager.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a message to be sent to one or more destinations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    /**
     * The text content of the message
     */
    private String text;
    
    /**
     * List of destinations where the message should be sent
     * Examples: "minecraft", "discord", "slack"
     */
    private List<String> destinations;
}
