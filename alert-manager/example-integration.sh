#!/bin/bash
# Example script demonstrating how to send alerts to the alert-manager
# This can be used from other modules like backup-manager or web-app

# Alert manager endpoint
ALERT_MANAGER_URL="${ALERT_MANAGER_URL:-http://alert-manager:8090/api/alerts}"

# Function to send an alert
send_alert() {
    local title="$1"
    local message="$2"
    local level="${3:-INFO}"
    local source="${4:-script}"
    
    curl -X POST "$ALERT_MANAGER_URL" \
      -H "Content-Type: application/json" \
      -s -o /dev/null -w "%{http_code}" \
      -d "{\"title\":\"$title\",\"message\":\"$message\",\"level\":\"$level\",\"source\":\"$source\"}"
}

# Example usage:
echo "Testing alert-manager integration..."

# Send an INFO alert
echo -n "Sending INFO alert... "
response=$(send_alert "Test Info Alert" "This is a test info message" "INFO" "test-script")
if [ "$response" = "200" ]; then
    echo "✓ Success"
else
    echo "✗ Failed (HTTP $response)"
fi

# Send a WARNING alert
echo -n "Sending WARNING alert... "
response=$(send_alert "Test Warning Alert" "This is a test warning message" "WARNING" "test-script")
if [ "$response" = "200" ]; then
    echo "✓ Success"
else
    echo "✗ Failed (HTTP $response)"
fi

# Send an ERROR alert
echo -n "Sending ERROR alert... "
response=$(send_alert "Test Error Alert" "This is a test error message" "ERROR" "test-script")
if [ "$response" = "200" ]; then
    echo "✓ Success"
else
    echo "✗ Failed (HTTP $response)"
fi

# Send a CRITICAL alert
echo -n "Sending CRITICAL alert... "
response=$(send_alert "Test Critical Alert" "This is a test critical message" "CRITICAL" "test-script")
if [ "$response" = "200" ]; then
    echo "✓ Success"
else
    echo "✗ Failed (HTTP $response)"
fi

echo ""
echo "Integration test complete!"
echo "Check the alert-manager logs: docker logs open-mc-alert-manager"
