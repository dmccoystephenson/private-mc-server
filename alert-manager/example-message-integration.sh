#!/bin/bash
# Example script demonstrating how to send alerts via the alert-manager
# This shows how the minecraft-wrapper script sends messages to the Minecraft server

# Alert manager endpoint
# When running from host, use localhost. When running from another container, use alert-manager
ALERT_MANAGER_URL="${ALERT_MANAGER_URL:-http://localhost:8090}"

# Function to send an alert
send_alert() {
    local message="$1"
    local destinations="$2"
    local level="${3:-INFO}"
    local source="${4:-test-script}"
    
    curl -X POST "$ALERT_MANAGER_URL/api/alerts" \
      -H "Content-Type: application/json" \
      -s -o /dev/null -w "%{http_code}" \
      -d "{\"message\":\"$message\",\"destinations\":$destinations,\"level\":\"$level\",\"source\":\"$source\"}"
}

# Example usage:
echo "Testing alert-manager integration..."

# Send a message to Minecraft server only
echo -n "Sending message to Minecraft server... "
response=$(send_alert "Hello from alert-manager!" '["MINECRAFT"]')
if [ "$response" = "200" ]; then
    echo "✓ Success"
else
    echo "✗ Failed (HTTP $response)"
fi

# Send a message to Discord only
echo -n "Sending alert to Discord... "
response=$(send_alert "This is a test Discord alert" '["DISCORD"]' "INFO" "test-script")
if [ "$response" = "200" ]; then
    echo "✓ Success"
else
    echo "✗ Failed (HTTP $response)"
fi

# Send to all destinations (omit destinations field)
echo -n "Sending alert to all destinations... "
response=$(curl -X POST "$ALERT_MANAGER_URL/api/alerts" \
  -H "Content-Type: application/json" \
  -s -o /dev/null -w "%{http_code}" \
  -d '{"message":"This goes everywhere","level":"INFO","source":"test-script"}')
if [ "$response" = "200" ]; then
    echo "✓ Success"
else
    echo "✗ Failed (HTTP $response)"
fi

echo ""
echo "Integration test complete!"
echo "Check the alert-manager logs: docker logs open-mc-alert-manager"
echo "Check the Minecraft server console if RCON is enabled"
echo "Check Discord if webhooks are configured"
