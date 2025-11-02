package com.openmc.alertmanager.controller;

import com.openmc.alertmanager.model.Alert;
import com.openmc.alertmanager.service.AlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for receiving alerts from other modules
 */
@RestController
@RequestMapping("/api/alerts")
@Slf4j
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Endpoint for sending alerts
     *
     * @param alert The alert to send
     * @return Response indicating success or failure
     */
    @PostMapping
    public ResponseEntity<String> sendAlert(@RequestBody Alert alert) {
        log.info("Received alert via API: {} from source: {}", alert.getTitle(), alert.getSource());
        
        try {
            alertService.sendAlert(alert);
            return ResponseEntity.ok("Alert sent successfully");
        } catch (Exception e) {
            log.error("Failed to send alert", e);
            return ResponseEntity.internalServerError().body("Failed to send alert: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     *
     * @return Status response
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Alert Manager is running");
    }
}
