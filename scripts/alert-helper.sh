#!/bin/bash
# Alert Helper Functions
# Common functions for sending alerts to the alert-manager service

# Function to send an alert to the alert-manager
# Usage: send_alert "title" "message" "level" "source"
# Levels: INFO, WARNING, ERROR, CRITICAL
send_alert() {
    local title="$1"
    local message="$2"
    local level="${3:-INFO}"
    local source="${4:-script}"
    
    # Determine the alert manager URL based on environment
    local alert_url
    if [ -n "${ALERT_MANAGER_URL:-}" ]; then
        # Use explicitly configured URL if set
        alert_url="$ALERT_MANAGER_URL"
    elif [ -f /.dockerenv ] || grep -q docker /proc/1/cgroup 2>/dev/null; then
        # Running in Docker container - use internal service name
        alert_url="http://alert-manager:8090/api/alerts"
    else
        # Running on host - use localhost
        alert_url="http://localhost:8090/api/alerts"
    fi
    
    # Try to send the alert, but don't fail if it doesn't work
    # This ensures that the main script continues even if alerting is down
    if command -v curl >/dev/null 2>&1; then
        curl -X POST "$alert_url" \
          -H "Content-Type: application/json" \
          -s -o /dev/null -w "" \
          --max-time 5 \
          --connect-timeout 5 \
          -d "{\"title\":\"$title\",\"message\":\"$message\",\"level\":\"$level\",\"source\":\"$source\"}" \
          2>/dev/null || true
    fi
}

# Export the function so it can be used by scripts that source this file
export -f send_alert
