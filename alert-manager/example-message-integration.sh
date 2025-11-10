#!/bin/bash
# Example script demonstrating how to send messages via the alert-manager
# This shows how the minecraft-wrapper script sends messages to the Minecraft server

# Alert manager endpoint
# When running from host, use localhost. When running from another container, use alert-manager
ALERT_MANAGER_URL="${ALERT_MANAGER_URL:-http://localhost:8090}"

# Function to send a message
send_message() {
    local text="$1"
    local destinations="$2"
    
    curl -X POST "$ALERT_MANAGER_URL/api/messages" \
      -H "Content-Type: application/json" \
      -s -o /dev/null -w "%{http_code}" \
      -d "{\"text\":\"$text\",\"destinations\":$destinations}"
}

# Example usage:
echo "Testing alert-manager message integration..."

# Send a message to Minecraft server
echo -n "Sending message to Minecraft server... "
response=$(send_message "Hello from alert-manager!" '["minecraft"]')
if [ "$response" = "200" ]; then
    echo "✓ Success"
else
    echo "✗ Failed (HTTP $response)"
fi

# Send a message to multiple destinations (future use)
echo -n "Sending message to multiple destinations... "
response=$(send_message "This is a test message" '["minecraft"]')
if [ "$response" = "200" ]; then
    echo "✓ Success"
else
    echo "✗ Failed (HTTP $response)"
fi

echo ""
echo "Integration test complete!"
echo "Check the alert-manager logs: docker logs open-mc-alert-manager"
echo "Check the Minecraft server console if RCON is enabled"
