@echo off
REM Build script for RSPS Command Center Bot (Windows)

echo Building RSPS Command Center Bot...
echo.

REM Clean and package
call mvn clean package

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful!
    echo Skeleton JAR: game-api-skeleton\target\game-api-skeleton-1.0.0.jar
    echo Bot JAR: bot\target\command-center-bot-1.0.0.jar
    echo.
    echo To run the bot:
    echo   java -jar bot\target\command-center-bot-1.0.0.jar
    echo.
    echo Or use the run script:
    echo   run.bat
) else (
    echo Build failed!
    exit /b 1
)

pause
