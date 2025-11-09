package com.openmc.alertmanager.model.discord;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO representing a Discord embed object
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiscordEmbed {
    
    private String title;
    private String description;
    private String url;
    private String timestamp;
    private Integer color;
    private DiscordEmbedFooter footer;
    private DiscordEmbedImage image;
    private DiscordEmbedThumbnail thumbnail;
    private DiscordEmbedAuthor author;
    private List<DiscordEmbedField> fields;
}
