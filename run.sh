#!/bin/bash

# Run script for RSPS Command Center Bot

JAR_FILE="bot/target/command-center-bot-1.0.0.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "JAR file not found. Building..."
    ./build.sh
fi

echo "Starting RSPS Command Center Bot..."
echo ""

# Check if bot.properties exists
if [ ! -f "bot/src/main/resources/bot.properties" ]; then
    echo "WARNING: bot.properties not found!"
    echo "Please copy bot.properties.example to bot.properties and configure it."
    echo ""
    echo "  cp bot/src/main/resources/bot.properties.example bot/src/main/resources/bot.properties"
    echo "  nano bot/src/main/resources/bot.properties"
    echo ""
    exit 1
fi

# Run the bot
java -jar "$JAR_FILE"
