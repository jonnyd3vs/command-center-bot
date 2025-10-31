FROM openjdk:11-jre-slim

# Set working directory
WORKDIR /app

# Copy the JAR file
COPY target/command-center-bot-1.0.0.jar app.jar

# Copy configuration files
COPY src/main/resources/servers.xml /app/servers.xml

# Create logs directory
RUN mkdir -p /app/logs

# Expose any necessary ports (optional)
# EXPOSE 8080

# Set environment variables (override with -e or docker-compose)
ENV DISCORD_BOT_TOKEN=""
ENV RSPS_API_KEY=""

# Run the bot
CMD ["java", "-jar", "app.jar"]
