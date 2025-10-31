@echo off
REM Run script for RSPS Command Center Bot (Windows)

set JAR_FILE=target\command-center-bot-1.0.0.jar

if not exist "%JAR_FILE%" (
    echo JAR file not found. Building...
    call build.bat
)

echo Starting RSPS Command Center Bot...
echo.

REM Check if bot.properties exists
if not exist "src\main\resources\bot.properties" (
    echo WARNING: bot.properties not found!
    echo Please copy bot.properties.example to bot.properties and configure it.
    echo.
    echo   copy src\main\resources\bot.properties.example src\main\resources\bot.properties
    echo   notepad src\main\resources\bot.properties
    echo.
    pause
    exit /b 1
)

REM Run the bot
java -jar "%JAR_FILE%"

pause
