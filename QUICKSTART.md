# Quick Start Guide

Get your RSPS Command Center Discord Bot up and running in 5 minutes!

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Discord bot token
- RSPS API key

## Quick Setup (Windows)

1. **Configure the bot**
   ```batch
   copy src\main\resources\bot.properties.example src\main\resources\bot.properties
   notepad src\main\resources\bot.properties
   ```

   Fill in:
   - `discord.bot.token` - Your Discord bot token
   - `api.key` - Your RSPS API key

2. **Configure servers**
   ```batch
   notepad src\main\resources\servers.xml
   ```

   Update the `<channelId>` tags with your Discord channel IDs.

3. **Build and run**
   ```batch
   build.bat
   run.bat
   ```

## Quick Setup (Linux/Mac)

1. **Configure the bot**
   ```bash
   cp src/main/resources/bot.properties.example src/main/resources/bot.properties
   nano src/main/resources/bot.properties
   ```

   Fill in:
   - `discord.bot.token` - Your Discord bot token
   - `api.key` - Your RSPS API key

2. **Configure servers**
   ```bash
   nano src/main/resources/servers.xml
   ```

   Update the `<channelId>` tags with your Discord channel IDs.

3. **Build and run**
   ```bash
   chmod +x build.sh run.sh
   ./build.sh
   ./run.sh
   ```

## Getting Your Discord Bot Token

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Click "New Application"
3. Give it a name (e.g., "RSPS Command Center")
4. Go to "Bot" section
5. Click "Add Bot"
6. Under "Token", click "Copy" to copy your bot token
7. **Important**: Enable these Privileged Gateway Intents:
   - Server Members Intent
   - Message Content Intent

## Inviting the Bot to Your Server

Use this URL format (replace YOUR_CLIENT_ID):
```
https://discord.com/api/oauth2/authorize?client_id=YOUR_CLIENT_ID&permissions=277025508352&scope=bot%20applications.commands
```

To get your client ID:
1. Go to Discord Developer Portal
2. Select your application
3. Copy the "Application ID" from the General Information page

## Getting Discord Channel/Role IDs

1. Enable Developer Mode:
   - User Settings → Advanced → Developer Mode (toggle on)

2. Get Channel ID:
   - Right-click any channel → Copy ID

3. Get Role ID:
   - Server Settings → Roles → Right-click role → Copy ID

## First Test

Once the bot is running and in your server:

1. Type `/` in any channel
2. You should see these commands:
   - `/giveitem`
   - `/massban`
   - `/giveoffers`
   - `/setpassword`

3. Try a test command:
   ```
   /giveitem server:VoidX player:TestPlayer itemid:995 amount:1000
   ```

## Troubleshooting

### "Discord bot token not configured"
- Make sure you copied `bot.properties.example` to `bot.properties`
- Make sure you filled in the `discord.bot.token` value

### "Commands not showing up"
- Wait up to 1 hour for global commands to register
- Make sure bot has "applications.commands" scope when invited
- Try kicking and re-inviting the bot

### "Permission Denied"
- Make sure you have the correct role assigned
- Configure `admin.role.id` and `moderator.role.id` in bot.properties
- Or make sure you have Administrator permission in Discord

## Next Steps

- Read the full [README.md](README.md) for detailed information
- See [DEPLOYMENT.md](DEPLOYMENT.md) for production deployment on Ubuntu
- Configure your game server API endpoints in `servers.xml`

## Need Help?

Check the troubleshooting sections in README.md and DEPLOYMENT.md, or contact the development team.
