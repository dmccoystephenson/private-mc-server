# Alert Manager

An alert notification system for the Minecraft server infrastructure. This Spring Boot application runs as a separate container and handles sending notifications to various destinations, starting with Discord. Other modules (backup-manager, webapp, etc.) can use this module to send alerts to server administrators and community members.

## Features

- **Discord Notifications**: Send alerts to Discord channels via webhooks
- **Multiple Alert Levels**: Support for INFO, WARNING, ERROR, and CRITICAL alert levels
- **REST API**: Easy integration with other modules via HTTP endpoints
- **Containerized**: Runs in its own Docker container for isolation
- **Extensible**: Designed to support additional notification destinations (Slack, Email, SMS, etc.)

## Configuration

### Environment Variables

The following environment variables can be configured in `.env`:

- `ALERT_CONTAINER_NAME`: Container name (default: `open-mc-alert-manager`)
- `ALERT_PORT`: Port for the alert manager API (default: `8090`)
- `DISCORD_WEBHOOK_URL`: Discord webhook URL for sending notifications
- `DISCORD_ENABLED`: Enable/disable Discord notifications (default: `false`)

### Discord Webhook Setup

To enable Discord notifications:

1. Create a webhook in your Discord server:
   - Go to Server Settings → Integrations → Webhooks
   - Click "New Webhook"
   - Choose a channel and copy the webhook URL
   
2. Add the webhook URL to your `.env` file:
   ```bash
   DISCORD_WEBHOOK_URL=https://discord.com/api/webhooks/YOUR_WEBHOOK_URL
   DISCORD_ENABLED=true
   ```

3. Restart the infrastructure: `./up.sh`

## Alert Levels

The alert manager supports four severity levels:

- **INFO**: Informational messages (blue in Discord)
- **WARNING**: Warning messages that require attention (yellow in Discord)
- **ERROR**: Error messages for failures (red in Discord)
- **CRITICAL**: Critical issues requiring immediate attention (dark red in Discord)

## API Endpoints

### Send Alert

**POST** `/api/alerts`

Send an alert notification.

Request body:
```json
{
  "title": "Backup Completed",
  "message": "Server backup completed successfully at 2 AM",
  "level": "INFO",
  "source": "backup-manager"
}
```

Response:
```
Alert sent successfully
```

### Health Check

**GET** `/api/alerts/health`

Check if the alert manager is running.

Response:
```
Alert Manager is running
```

## Usage Examples

### From Command Line (curl)

```bash
curl -X POST http://alert-manager:8090/api/alerts \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Server Started",
    "message": "Minecraft server has started successfully",
    "level": "INFO",
    "source": "mcserver"
  }'
```

### From Java/Spring Boot

```java
@Service
public class MyService {
    private final RestTemplate restTemplate;
    
    public void sendAlert(String title, String message, String level) {
        Map<String, String> alert = new HashMap<>();
        alert.put("title", title);
        alert.put("message", message);
        alert.put("level", level);
        alert.put("source", "my-module");
        
        restTemplate.postForObject(
            "http://alert-manager:8090/api/alerts",
            alert,
            String.class
        );
    }
}
```

### From Shell Script

```bash
#!/bin/bash
send_alert() {
    local title="$1"
    local message="$2"
    local level="${3:-INFO}"
    
    curl -X POST http://alert-manager:8090/api/alerts \
      -H "Content-Type: application/json" \
      -d "{\"title\":\"$title\",\"message\":\"$message\",\"level\":\"$level\",\"source\":\"script\"}"
}

send_alert "Backup Failed" "Backup process encountered an error" "ERROR"
```

## Building

Build the alert-manager application:

```bash
cd alert-manager
./build.sh
```

Or manually:

```bash
./gradlew clean build
```

## Testing

Run tests:

```bash
./gradlew test
```

### Integration Testing

An example integration script is provided to test the alert-manager from the host machine:

```bash
./example-integration.sh
```

This script sends alerts to `http://localhost:8090/api/alerts` by default. To use it from within another container, set the `ALERT_MANAGER_URL` environment variable:

```bash
# From host machine (default)
./example-integration.sh

# From within a container
ALERT_MANAGER_URL=http://alert-manager:8090/api/alerts ./example-integration.sh
```

The script demonstrates how to send alerts from other modules and verifies that the alert-manager API is working correctly.

## Running

The alert-manager is automatically started with the rest of the infrastructure:

```bash
cd ..
./up.sh
```

### Viewing Logs

To view alert-manager logs:

```bash
docker logs -f open-mc-alert-manager
```

Or use your custom container name:

```bash
docker logs -f ${ALERT_CONTAINER_NAME}
```

## Integration with Other Modules

The alert-manager is designed to be used by other modules in the infrastructure:

### Backup Manager Integration

The backup-manager can send alerts when backups complete or fail:

```java
@Service
public class BackupService {
    @Autowired
    private RestTemplate restTemplate;
    
    public void performBackup() {
        try {
            // ... backup logic ...
            sendAlert("Backup Completed", "Server backup completed successfully", "INFO");
        } catch (Exception e) {
            sendAlert("Backup Failed", "Error: " + e.getMessage(), "ERROR");
        }
    }
    
    private void sendAlert(String title, String message, String level) {
        Alert alert = Alert.builder()
            .title(title)
            .message(message)
            .level(AlertLevel.valueOf(level))
            .source("backup-manager")
            .build();
        
        restTemplate.postForObject(
            "http://alert-manager:8090/api/alerts",
            alert,
            String.class
        );
    }
}
```

### Web Application Integration

The web application can send alerts for server events:

```java
@Service
public class ServerMonitorService {
    @Autowired
    private RestTemplate restTemplate;
    
    public void onServerStart() {
        sendAlert("Server Started", 
                 "Minecraft server has started successfully", 
                 "INFO");
    }
    
    public void onServerCrash(String error) {
        sendAlert("Server Crashed", 
                 "Server crashed with error: " + error, 
                 "CRITICAL");
    }
}
```

## Future Enhancements

The alert-manager is designed to be extensible. Future versions may include:

- **Slack Integration**: Send alerts to Slack channels
- **Email Notifications**: Send alerts via email
- **SMS Alerts**: Send critical alerts via SMS
- **Alert Filtering**: Configure which alert levels to send to each destination
- **Alert Scheduling**: Quiet hours for non-critical alerts
- **Alert Grouping**: Group similar alerts to reduce noise
- **Webhook Retries**: Retry failed webhook deliveries

## Architecture

```
┌─────────────────┐
│  Backup Manager │──┐
└─────────────────┘  │
                     │
┌─────────────────┐  │    ┌──────────────────┐
│   Web App       │──┼───→│  Alert Manager   │
└─────────────────┘  │    │                  │
                     │    │  - REST API      │
┌─────────────────┐  │    │  - Discord       │
│  Other Modules  │──┘    │  - (Future...)   │
└─────────────────┘       └──────────────────┘
                                    │
                                    ↓
                          ┌──────────────────┐
                          │    Discord       │
                          │    Server        │
                          └──────────────────┘
```

## Security Notes

- Store webhook URLs securely in environment variables, never in code
- Consider using a reverse proxy with authentication for production deployments
- Monitor alert volume to prevent spam or abuse
- Regularly rotate webhook URLs if compromised

## Troubleshooting

### Alerts Not Being Sent

1. Check container logs: `docker logs open-mc-alert-manager`
2. Verify Discord is enabled: `DISCORD_ENABLED=true` in `.env`
3. Verify webhook URL is correct in `.env`
4. Ensure the container is running: `docker ps | grep alert-manager`

### Discord Webhook Fails

1. Verify the webhook URL is still valid in Discord
2. Check Discord server permissions
3. Ensure the webhook hasn't been deleted
4. Check for rate limiting in the logs

### Can't Connect to Alert Manager API

1. Verify the container is running and healthy
2. Check the port mapping in `docker-compose.yml`
3. Test with curl: `curl http://alert-manager:8090/api/alerts/health`
4. Check network connectivity between containers
