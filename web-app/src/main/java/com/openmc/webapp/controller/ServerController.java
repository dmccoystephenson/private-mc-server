package com.openmc.webapp.controller;

import com.openmc.webapp.config.ServerConfig;
import com.openmc.webapp.model.ActivityTrackerStats;
import com.openmc.webapp.model.LeaderboardEntry;
import com.openmc.webapp.service.ActivityTrackerService;
import com.openmc.webapp.service.RconService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class ServerController {
    
    private static final Logger logger = LoggerFactory.getLogger(ServerController.class);
    
    private final RconService rconService;
    private final ServerConfig serverConfig;
    private final ActivityTrackerService activityTrackerService;
    
    public ServerController(RconService rconService, ServerConfig serverConfig, 
                          ActivityTrackerService activityTrackerService) {
        this.rconService = rconService;
        this.serverConfig = serverConfig;
        this.activityTrackerService = activityTrackerService;
    }
    
    @GetMapping("/")
    public String index() {
        return "redirect:/public";
    }
    
    @GetMapping("/public")
    public String publicPage(Model model) {
        RconService.ServerStatus status = rconService.getServerStatus();
        model.addAttribute("status", status);
        model.addAttribute("dynmapUrl", serverConfig.getDynmapUrl());
        model.addAttribute("bluemapUrl", serverConfig.getBluemapUrl());
        model.addAttribute("refreshIntervalMs", serverConfig.getRefreshIntervalMs());
        model.addAttribute("lastFetchTime", rconService.getLastFetchTime());
        model.addAttribute("activityTrackerEnabled", activityTrackerService.isEnabled());
        model.addAttribute("dashboardTitle", serverConfig.getDashboardTitle());
        model.addAttribute("dashboardSubtitle", serverConfig.getDashboardSubtitle());
        model.addAttribute("dashboardPrimaryColor", serverConfig.getDashboardPrimaryColor());
        model.addAttribute("dashboardSecondaryColor", serverConfig.getDashboardSecondaryColor());
        return "public";
    }
    
    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("dashboardTitle", serverConfig.getDashboardTitle());
        model.addAttribute("dashboardSubtitle", serverConfig.getDashboardSubtitle());
        model.addAttribute("dashboardPrimaryColor", serverConfig.getDashboardPrimaryColor());
        model.addAttribute("dashboardSecondaryColor", serverConfig.getDashboardSecondaryColor());
        return "login";
    }
    
    @GetMapping("/admin")
    public String adminPage(HttpSession session, Model model) {
        // Check if user is authenticated
        Boolean isAuthenticated = (Boolean) session.getAttribute("authenticated");
        if (isAuthenticated == null || !isAuthenticated) {
            return "redirect:/login";
        }
        
        model.addAttribute("dashboardTitle", serverConfig.getDashboardTitle());
        model.addAttribute("dashboardSubtitle", serverConfig.getDashboardSubtitle());
        model.addAttribute("dashboardPrimaryColor", serverConfig.getDashboardPrimaryColor());
        model.addAttribute("dashboardSecondaryColor", serverConfig.getDashboardSecondaryColor());
        return "admin";
    }
    
    @PostMapping("/api/command")
    @ResponseBody
    public Map<String, String> sendCommand(@RequestBody Map<String, String> payload, HttpSession session) {
        // Check if user is authenticated
        Boolean isAuthenticated = (Boolean) session.getAttribute("authenticated");
        if (isAuthenticated == null || !isAuthenticated) {
            return Map.of("result", "Error: You must be logged in to execute commands");
        }
        
        String command = payload.get("command");
        
        // Validate command
        if (command == null || command.trim().isEmpty()) {
            return Map.of("result", "Error: Command cannot be empty");
        }
        
        String result = rconService.sendCommand(command);
        return Map.of("result", result);
    }
    
    @GetMapping("/api/status")
    @ResponseBody
    public RconService.ServerStatus getStatus() {
        return rconService.getServerStatus();
    }
    
    @GetMapping("/api/resources")
    @ResponseBody
    public RconService.ResourceUsage getResources() {
        return rconService.getResourceUsage();
    }
    
    @GetMapping("/api/history")
    @ResponseBody
    public Map<String, Object> getHistory() {
        return Map.of("history", rconService.getRetrievalHistory());
    }
    
    @GetMapping("/api/activity-tracker/stats")
    @ResponseBody
    public ActivityTrackerStats getActivityTrackerStats() {
        logger.debug("API request: /api/activity-tracker/stats");
        ActivityTrackerStats stats = activityTrackerService.getStats();
        if (stats == null) {
            logger.warn("Activity Tracker stats request returned null - check if integration is enabled and API is accessible");
        }
        return stats;
    }
    
    @GetMapping("/api/activity-tracker/leaderboard")
    @ResponseBody
    public List<LeaderboardEntry> getActivityTrackerLeaderboard() {
        logger.debug("API request: /api/activity-tracker/leaderboard");
        List<LeaderboardEntry> leaderboard = activityTrackerService.getLeaderboard();
        if (leaderboard.isEmpty()) {
            logger.warn("Activity Tracker leaderboard request returned empty - check if integration is enabled and API is accessible");
        }
        return leaderboard;
    }
    
    @GetMapping("/api/activity-tracker/enabled")
    @ResponseBody
    public Map<String, Boolean> getActivityTrackerEnabled() {
        boolean enabled = activityTrackerService.isEnabled();
        logger.debug("API request: /api/activity-tracker/enabled - returning: {}", enabled);
        return Map.of("enabled", enabled);
    }
    
    // Authentication Endpoints
    
    @PostMapping("/api/login")
    @ResponseBody
    public Map<String, Object> login(@RequestBody Map<String, String> payload, HttpSession session) {
        String username = payload.get("username");
        String password = payload.get("password");
        
        if (username == null || password == null) {
            return Map.of("success", false, "message", "Username and password are required");
        }
        
        if (serverConfig.getAdminUsername().equals(username) && 
            serverConfig.getAdminPassword().equals(password)) {
            session.setAttribute("authenticated", true);
            session.setAttribute("username", username);
            logger.info("User {} successfully logged in", username);
            return Map.of("success", true, "message", "Login successful");
        } else {
            logger.warn("Failed login attempt for username: {}", username);
            return Map.of("success", false, "message", "Invalid username or password");
        }
    }
    
    @PostMapping("/api/logout")
    @ResponseBody
    public Map<String, Object> logout(HttpSession session) {
        String username = (String) session.getAttribute("username");
        session.invalidate();
        logger.info("User {} logged out", username);
        return Map.of("success", true, "message", "Logged out successfully");
    }
    
    // Allow/Deny List Management Endpoints
    
    private boolean isAuthenticated(HttpSession session) {
        Boolean authenticated = (Boolean) session.getAttribute("authenticated");
        return authenticated != null && authenticated;
    }
    
    @PostMapping("/api/whitelist/toggle")
    @ResponseBody
    public Map<String, String> toggleWhitelist(@RequestBody Map<String, String> payload, HttpSession session) {
        if (!isAuthenticated(session)) {
            return Map.of("result", "Error: You must be logged in to perform this action");
        }
        
        String action = payload.get("action"); // "on" or "off"
        
        if (action == null || (!action.equals("on") && !action.equals("off"))) {
            return Map.of("result", "Error: Action must be 'on' or 'off'");
        }
        
        String result = rconService.sendCommand("whitelist " + action);
        return Map.of("result", result);
    }
    
    @PostMapping("/api/whitelist/add")
    @ResponseBody
    public Map<String, String> addToWhitelist(@RequestBody Map<String, String> payload, HttpSession session) {
        if (!isAuthenticated(session)) {
            return Map.of("result", "Error: You must be logged in to perform this action");
        }
        
        String player = payload.get("player");
        
        if (player == null || player.trim().isEmpty()) {
            return Map.of("result", "Error: Player name is required");
        }
        
        String result = rconService.sendCommand("whitelist add " + player.trim());
        return Map.of("result", result);
    }
    
    @PostMapping("/api/whitelist/remove")
    @ResponseBody
    public Map<String, String> removeFromWhitelist(@RequestBody Map<String, String> payload, HttpSession session) {
        if (!isAuthenticated(session)) {
            return Map.of("result", "Error: You must be logged in to perform this action");
        }
        
        String player = payload.get("player");
        
        if (player == null || player.trim().isEmpty()) {
            return Map.of("result", "Error: Player name is required");
        }
        
        String result = rconService.sendCommand("whitelist remove " + player.trim());
        return Map.of("result", result);
    }
    
    @PostMapping("/api/whitelist/list")
    @ResponseBody
    public Map<String, String> listWhitelist(HttpSession session) {
        if (!isAuthenticated(session)) {
            return Map.of("result", "Error: You must be logged in to perform this action");
        }
        
        String result = rconService.sendCommand("whitelist list");
        return Map.of("result", result);
    }
    
    @PostMapping("/api/ban/add")
    @ResponseBody
    public Map<String, String> banPlayer(@RequestBody Map<String, String> payload, HttpSession session) {
        if (!isAuthenticated(session)) {
            return Map.of("result", "Error: You must be logged in to perform this action");
        }
        
        String player = payload.get("player");
        String reason = payload.get("reason");
        
        if (player == null || player.trim().isEmpty()) {
            return Map.of("result", "Error: Player name is required");
        }
        
        String command = "ban " + player.trim();
        if (reason != null && !reason.trim().isEmpty()) {
            command += " " + reason.trim();
        }
        
        String result = rconService.sendCommand(command);
        return Map.of("result", result);
    }
    
    @PostMapping("/api/ban/remove")
    @ResponseBody
    public Map<String, String> unbanPlayer(@RequestBody Map<String, String> payload, HttpSession session) {
        if (!isAuthenticated(session)) {
            return Map.of("result", "Error: You must be logged in to perform this action");
        }
        
        String player = payload.get("player");
        
        if (player == null || player.trim().isEmpty()) {
            return Map.of("result", "Error: Player name is required");
        }
        
        String result = rconService.sendCommand("pardon " + player.trim());
        return Map.of("result", result);
    }
    
    @PostMapping("/api/ban/list")
    @ResponseBody
    public Map<String, String> listBans(HttpSession session) {
        if (!isAuthenticated(session)) {
            return Map.of("result", "Error: You must be logged in to perform this action");
        }
        
        String result = rconService.sendCommand("banlist");
        return Map.of("result", result);
    }
}
