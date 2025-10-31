#!/bin/bash

# Build script for RSPS Command Center Bot

echo "Building RSPS Command Center Bot..."

# Clean and package
mvn clean package

if [ $? -eq 0 ]; then
    echo ""
    echo "Build successful!"
    echo "Skeleton JAR: game-api-skeleton/target/game-api-skeleton-1.0.0.jar"
    echo "Bot JAR: bot/target/command-center-bot-1.0.0.jar"
    echo ""
    echo "To run the bot:"
    echo "  java -jar bot/target/command-center-bot-1.0.0.jar"
    echo ""
    echo "Or use the run script:"
    echo "  ./run.sh"
else
    echo "Build failed!"
    exit 1
fi
