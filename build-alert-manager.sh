#!/bin/bash
# Build script for the alert-manager

set -e

echo "Building alert-manager..."
cd "$(dirname "$0")/alert-manager"

# Build the Spring Boot application
./gradlew clean build -x test

echo "✓ Alert-manager built successfully"
echo "JAR file: alert-manager/build/libs/alert-manager-0.0.1.jar"
