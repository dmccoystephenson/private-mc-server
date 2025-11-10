package com.openmc.webapp.controller;

import com.openmc.webapp.config.ServerConfig;
import com.openmc.webapp.service.ActivityTrackerService;
import com.openmc.webapp.service.RconService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(ServerController.class)
@DisplayName("ServerController Tests")
class ServerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RconService rconService;

    @MockBean
    private ServerConfig serverConfig;
    
    @MockBean
    private ActivityTrackerService activityTrackerService;

    private RconService.ServerStatus mockStatus;

    @BeforeEach
    void setUp() {
        RconService.ResourceUsage mockResourceUsage = new RconService.ResourceUsage("20.0, 20.0, 20.0", "1024MB", "2048MB", "1024MB", 50.0);
        mockStatus = new RconService.ServerStatus(serverConfig, "There are 0 of a max of 20 players online", mockResourceUsage);
        
        when(serverConfig.getMotd()).thenReturn("Test Server");
        when(serverConfig.getMaxPlayers()).thenReturn(20);
        when(serverConfig.getDynmapUrl()).thenReturn("");
        when(serverConfig.getBluemapUrl()).thenReturn("");
        when(serverConfig.getAdminUsername()).thenReturn("admin");
        when(serverConfig.getAdminPassword()).thenReturn("admin");
        when(activityTrackerService.isEnabled()).thenReturn(false);
    }

    @Test
    @DisplayName("Should redirect to /public on GET /")
    void shouldRedirectToPublicOnGetRoot() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/public"));
    }

    @Test
    @DisplayName("Should return public page on GET /public")
    void shouldReturnPublicPageOnGetPublic() throws Exception {
        when(rconService.getServerStatus()).thenReturn(mockStatus);

        mockMvc.perform(get("/public"))
                .andExpect(status().isOk())
                .andExpect(view().name("public"))
                .andExpect(model().attributeExists("status"))
                .andExpect(model().attributeExists("dynmapUrl"))
                .andExpect(model().attributeExists("bluemapUrl"));
    }

    @Test
    @DisplayName("Should return admin page on GET /admin")
    void shouldReturnAdminPageOnGetAdmin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin"));
    }

    @Test
    @DisplayName("Should return server status on GET /api/status")
    void shouldReturnServerStatusOnGetApiStatus() throws Exception {
        when(rconService.getServerStatus()).thenReturn(mockStatus);

        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should return resource usage on GET /api/resources")
    void shouldReturnResourceUsageOnGetApiResources() throws Exception {
        RconService.ResourceUsage mockResourceUsage = new RconService.ResourceUsage("20.0, 20.0, 20.0", "1024MB", "2048MB", "1024MB", 50.0);
        when(rconService.getResourceUsage()).thenReturn(mockResourceUsage);

        mockMvc.perform(get("/api/resources"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tps").value("20.0, 20.0, 20.0"))
                .andExpect(jsonPath("$.memoryUsed").value("1024MB"));
    }

    @Test
    @DisplayName("Should accept valid command with authentication")
    void shouldAcceptValidCommandWithAuthentication() throws Exception {
        when(rconService.sendCommand("list")).thenReturn("There are 0 of a max of 20 players online");

        mockMvc.perform(post("/api/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\",\"command\":\"list\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());
    }

    @Test
    @DisplayName("Should reject command without username")
    void shouldRejectCommandWithoutUsername() throws Exception {
        mockMvc.perform(post("/api/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"admin\",\"command\":\"list\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(containsString("Username and password are required")));
    }

    @Test
    @DisplayName("Should reject command without password")
    void shouldRejectCommandWithoutPassword() throws Exception {
        mockMvc.perform(post("/api/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"command\":\"list\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(containsString("Username and password are required")));
    }

    @Test
    @DisplayName("Should reject command with invalid credentials")
    void shouldRejectCommandWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"wrong\",\"password\":\"wrong\",\"command\":\"list\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(containsString("Invalid username or password")));
    }

    @Test
    @DisplayName("Should reject empty command")
    void shouldRejectEmptyCommand() throws Exception {
        mockMvc.perform(post("/api/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\",\"command\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(containsString("Command cannot be empty")));
    }

    @Test
    @DisplayName("Should reject null command")
    void shouldRejectNullCommand() throws Exception {
        mockMvc.perform(post("/api/command")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(containsString("Command cannot be empty")));
    }

    // Whitelist Management Tests
    
    @Test
    @DisplayName("Should enable whitelist with valid credentials")
    void shouldEnableWhitelistWithValidCredentials() throws Exception {
        when(rconService.sendCommand("whitelist on")).thenReturn("Turned on the whitelist");

        mockMvc.perform(post("/api/whitelist/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\",\"action\":\"on\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Turned on the whitelist"));
    }

    @Test
    @DisplayName("Should disable whitelist with valid credentials")
    void shouldDisableWhitelistWithValidCredentials() throws Exception {
        when(rconService.sendCommand("whitelist off")).thenReturn("Turned off the whitelist");

        mockMvc.perform(post("/api/whitelist/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\",\"action\":\"off\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Turned off the whitelist"));
    }

    @Test
    @DisplayName("Should reject whitelist toggle with invalid credentials")
    void shouldRejectWhitelistToggleWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/whitelist/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"wrong\",\"password\":\"wrong\",\"action\":\"on\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(containsString("Invalid credentials")));
    }

    @Test
    @DisplayName("Should reject whitelist toggle with invalid action")
    void shouldRejectWhitelistToggleWithInvalidAction() throws Exception {
        mockMvc.perform(post("/api/whitelist/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\",\"action\":\"invalid\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(containsString("Action must be 'on' or 'off'")));
    }

    @Test
    @DisplayName("Should add player to whitelist with valid credentials")
    void shouldAddPlayerToWhitelistWithValidCredentials() throws Exception {
        when(rconService.sendCommand("whitelist add TestPlayer")).thenReturn("Added TestPlayer to the whitelist");

        mockMvc.perform(post("/api/whitelist/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\",\"player\":\"TestPlayer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Added TestPlayer to the whitelist"));
    }

    @Test
    @DisplayName("Should remove player from whitelist with valid credentials")
    void shouldRemovePlayerFromWhitelistWithValidCredentials() throws Exception {
        when(rconService.sendCommand("whitelist remove TestPlayer")).thenReturn("Removed TestPlayer from the whitelist");

        mockMvc.perform(post("/api/whitelist/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\",\"player\":\"TestPlayer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Removed TestPlayer from the whitelist"));
    }

    @Test
    @DisplayName("Should list whitelist with valid credentials")
    void shouldListWhitelistWithValidCredentials() throws Exception {
        when(rconService.sendCommand("whitelist list")).thenReturn("There are 2 whitelisted players: Player1, Player2");

        mockMvc.perform(post("/api/whitelist/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("There are 2 whitelisted players: Player1, Player2"));
    }

    @Test
    @DisplayName("Should reject whitelist operations with invalid credentials")
    void shouldRejectWhitelistOperationsWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/whitelist/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"wrong\",\"password\":\"wrong\",\"player\":\"TestPlayer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(containsString("Invalid credentials")));
    }

    @Test
    @DisplayName("Should reject whitelist add without player name")
    void shouldRejectWhitelistAddWithoutPlayerName() throws Exception {
        mockMvc.perform(post("/api/whitelist/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\",\"player\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(containsString("Player name is required")));
    }

    // Ban Management Tests

    @Test
    @DisplayName("Should ban player with valid credentials")
    void shouldBanPlayerWithValidCredentials() throws Exception {
        when(rconService.sendCommand("ban TestPlayer")).thenReturn("Banned TestPlayer");

        mockMvc.perform(post("/api/ban/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\",\"player\":\"TestPlayer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Banned TestPlayer"));
    }

    @Test
    @DisplayName("Should ban player with reason")
    void shouldBanPlayerWithReason() throws Exception {
        when(rconService.sendCommand("ban TestPlayer Griefing")).thenReturn("Banned TestPlayer: Griefing");

        mockMvc.perform(post("/api/ban/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\",\"player\":\"TestPlayer\",\"reason\":\"Griefing\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Banned TestPlayer: Griefing"));
    }

    @Test
    @DisplayName("Should unban player with valid credentials")
    void shouldUnbanPlayerWithValidCredentials() throws Exception {
        when(rconService.sendCommand("pardon TestPlayer")).thenReturn("Unbanned TestPlayer");

        mockMvc.perform(post("/api/ban/remove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\",\"player\":\"TestPlayer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Unbanned TestPlayer"));
    }

    @Test
    @DisplayName("Should list banned players with valid credentials")
    void shouldListBannedPlayersWithValidCredentials() throws Exception {
        when(rconService.sendCommand("banlist")).thenReturn("There are 1 banned players: BadPlayer");

        mockMvc.perform(post("/api/ban/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("There are 1 banned players: BadPlayer"));
    }

    @Test
    @DisplayName("Should reject ban operations with invalid credentials")
    void shouldRejectBanOperationsWithInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/ban/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"wrong\",\"password\":\"wrong\",\"player\":\"TestPlayer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(containsString("Invalid credentials")));
    }

    @Test
    @DisplayName("Should reject ban without player name")
    void shouldRejectBanWithoutPlayerName() throws Exception {
        mockMvc.perform(post("/api/ban/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin\",\"player\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value(containsString("Player name is required")));
    }
}
