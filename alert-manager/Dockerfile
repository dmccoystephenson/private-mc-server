# Using Ubuntu-based image for reliable package installation
FROM eclipse-temurin:21-jre

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY build/libs/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
