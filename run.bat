@echo off
REM Run script for RSPS Command Center Bot (Windows)

set JAR_FILE=bot\target\command-center-bot-1.0.0.jar

if not exist "%JAR_FILE%" (
    echo JAR file not found. Building...
    call build.bat
)

echo Starting RSPS Command Center Bot...
echo.

REM Check if bot.properties exists
if not exist "bot\src\main\resources\bot.properties" (
    echo WARNING: bot.properties not found!
    echo Please copy bot.properties.example to bot.properties and configure it.
    echo.
    echo   copy bot\src\main\resources\bot.properties.example bot\src\main\resources\bot.properties
    echo   notepad bot\src\main\resources\bot.properties
    echo.
    pause
    exit /b 1
)

REM Run the bot
java -jar "%JAR_FILE%"

pause
