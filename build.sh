#!/bin/bash

# Build script for RSPS Command Center Bot

echo "Building RSPS Command Center Bot..."

# Clean and package
mvn clean package

if [ $? -eq 0 ]; then
    echo ""
    echo "Build successful!"
    echo "JAR file: target/command-center-bot-1.0.0.jar"
    echo ""
    echo "To run the bot:"
    echo "  java -jar target/command-center-bot-1.0.0.jar"
    echo ""
    echo "Or use the run script:"
    echo "  ./run.sh"
else
    echo "Build failed!"
    exit 1
fi
