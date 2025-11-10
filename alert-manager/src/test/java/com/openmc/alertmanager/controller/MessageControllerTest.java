package com.openmc.alertmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openmc.alertmanager.model.Message;
import com.openmc.alertmanager.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MessageController.class)
@DisplayName("MessageController Tests")
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    @Test
    @DisplayName("Should accept valid message request")
    void shouldAcceptValidMessageRequest() throws Exception {
        Message message = Message.builder()
            .text("Test message")
            .destinations(Collections.singletonList("minecraft"))
            .build();

        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(message)))
            .andExpect(status().isOk())
            .andExpect(content().string("Message sent successfully"));

        verify(messageService, times(1)).sendMessage(any(Message.class));
    }

    @Test
    @DisplayName("Should return 500 when service throws exception")
    void shouldReturn500WhenServiceThrowsException() throws Exception {
        doThrow(new RuntimeException("Service error")).when(messageService).sendMessage(any());

        Message message = Message.builder()
            .text("Test message")
            .destinations(Collections.singletonList("minecraft"))
            .build();

        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(message)))
            .andExpect(status().isInternalServerError())
            .andExpect(content().string("Failed to send message"));
    }

    @Test
    @DisplayName("Should accept message with multiple destinations")
    void shouldAcceptMessageWithMultipleDestinations() throws Exception {
        Message message = Message.builder()
            .text("Multi-destination message")
            .destinations(java.util.Arrays.asList("minecraft", "discord"))
            .build();

        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(message)))
            .andExpect(status().isOk());

        verify(messageService, times(1)).sendMessage(any(Message.class));
    }

    @Test
    @DisplayName("Should accept message with empty destinations")
    void shouldAcceptMessageWithEmptyDestinations() throws Exception {
        Message message = Message.builder()
            .text("No destinations")
            .destinations(Collections.emptyList())
            .build();

        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(message)))
            .andExpect(status().isOk());

        verify(messageService, times(1)).sendMessage(any(Message.class));
    }
}
