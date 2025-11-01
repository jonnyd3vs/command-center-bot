# RSPS Command Center Discord Bot

A Discord bot for managing multiple RuneScape Private Servers (RSPS) through a centralized command center. This bot allows administrators and moderators to execute management commands across multiple game servers directly from Discord.

<!-- Deployment trigger: 2025-01-01 -->

## Features

- **Multi-Server Management**: Control multiple RSPS instances from a single Discord server
- **Permission-Based Commands**: Role-based access control for different command levels
- **Slash Commands**: Modern Discord slash command interface
- **Secure Authentication**: API key authentication with game servers
- **Channel-Based Organization**: Each server can have its own dedicated Discord channel

## Available Commands

| Command | Description | Permission Level |
|---------|-------------|------------------|
| `/giveitem` | Give items to a player | Moderator |
| `/massban` | Ban a player and associated accounts | Admin |
| `/giveoffers` | Give special offers to a player | Moderator |
| `/setpassword` | Reset a player's password | Admin |

## Setup

### Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Discord Bot Token ([Get one here](https://discord.com/developers/applications))
- RSPS API Key (provided by your game server)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd command-center-bot
   ```

2. **Configure the bot**

   Copy the example configuration file:
   ```bash
   cp src/main/resources/bot.properties.example src/main/resources/bot.properties
   ```

   Edit `src/main/resources/bot.properties` and fill in your values:
   ```properties
   discord.bot.token=YOUR_DISCORD_BOT_TOKEN
   api.key=YOUR_RSPS_API_KEY
   admin.role.id=YOUR_ADMIN_ROLE_ID
   moderator.role.id=YOUR_MODERATOR_ROLE_ID
   ```

3. **Configure servers**

   Edit `src/main/resources/servers.xml` to add your game servers:
   ```xml
   <server>
       <name>ServerName</name>
       <host>server.ip.address</host>
       <port>8090</port>
       <channelId>DISCORD_CHANNEL_ID</channelId>
   </server>
   ```

4. **Build the project**
   ```bash
   mvn clean package
   ```

5. **Run the bot**
   ```bash
   java -jar target/command-center-bot-1.0.0.jar
   ```

## Configuration Options

### Environment Variables

Instead of using `bot.properties`, you can set environment variables:

- `DISCORD_BOT_TOKEN` - Discord bot token
- `RSPS_API_KEY` - API key for game servers
- `ADMIN_ROLE_ID` - Discord role ID for admin permissions
- `MODERATOR_ROLE_ID` - Discord role ID for moderator permissions
- `COMMAND_PREFIX` - Command prefix (default: !)

### Discord Bot Permissions

When inviting the bot to your server, make sure it has these permissions:
- Read Messages/View Channels
- Send Messages
- Use Slash Commands
- Embed Links

Bot invite URL format:
```
https://discord.com/api/oauth2/authorize?client_id=YOUR_BOT_CLIENT_ID&permissions=277025508352&scope=bot%20applications.commands
```

### Role Configuration

If `admin.role.id` and `moderator.role.id` are not set, the bot will use Discord's built-in permissions:
- **Admin commands**: Require Administrator permission
- **Moderator commands**: Require Manage Server permission

To get role IDs:
1. Enable Developer Mode in Discord (User Settings > Advanced)
2. Right-click the role and select "Copy ID"

## Deployment on Ubuntu Server

### Using systemd service

1. **Upload the JAR file to your server**
   ```bash
   scp target/command-center-bot-1.0.0.jar user@server:/opt/rsps-bot/
   ```

2. **Create a systemd service file**
   ```bash
   sudo nano /etc/systemd/system/rsps-bot.service
   ```

   Add the following content:
   ```ini
   [Unit]
   Description=RSPS Command Center Discord Bot
   After=network.target

   [Service]
   Type=simple
   User=YOUR_USER
   WorkingDirectory=/opt/rsps-bot
   ExecStart=/usr/bin/java -jar /opt/rsps-bot/command-center-bot-1.0.0.jar
   Restart=always
   RestartSec=10
   Environment="DISCORD_BOT_TOKEN=your_token_here"
   Environment="RSPS_API_KEY=your_api_key_here"

   [Install]
   WantedBy=multi-user.target
   ```

3. **Enable and start the service**
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable rsps-bot
   sudo systemctl start rsps-bot
   ```

4. **Check status**
   ```bash
   sudo systemctl status rsps-bot
   sudo journalctl -u rsps-bot -f
   ```

### Using Docker

1. **Create a Dockerfile**
   ```dockerfile
   FROM openjdk:11-jre-slim
   WORKDIR /app
   COPY target/command-center-bot-1.0.0.jar app.jar
   COPY src/main/resources/servers.xml servers.xml
   CMD ["java", "-jar", "app.jar"]
   ```

2. **Build and run**
   ```bash
   docker build -t rsps-bot .
   docker run -d \
     --name rsps-bot \
     -e DISCORD_BOT_TOKEN=your_token \
     -e RSPS_API_KEY=your_api_key \
     rsps-bot
   ```

## Project Structure

```
command-center-bot/
├── src/
│   └── main/
│       ├── java/com/rsps/discordbot/
│       │   ├── CommandCenterBot.java          # Main bot class
│       │   ├── client/
│       │   │   └── GameServerClient.java      # HTTP client for game servers
│       │   ├── commands/
│       │   │   ├── Command.java               # Command interface
│       │   │   ├── CommandManager.java        # Command registration & permissions
│       │   │   ├── GiveItemCommand.java       # Give item command
│       │   │   ├── MassBanCommand.java        # Mass ban command
│       │   │   ├── GiveOffersCommand.java     # Give offers command
│       │   │   └── SetPasswordCommand.java    # Set password command
│       │   └── config/
│       │       ├── BotConfig.java             # Bot configuration loader
│       │       └── ServerConfig.java          # Server configuration loader
│       └── resources/
│           ├── bot.properties.example         # Example configuration
│           ├── servers.xml                    # Server list
│           └── logback.xml                    # Logging configuration
├── pom.xml                                    # Maven configuration
└── README.md                                  # This file
```

## Troubleshooting

### Bot doesn't respond to commands
- Make sure slash commands are registered (can take up to 1 hour for global commands)
- Check bot permissions in Discord
- Verify the bot is online and connected

### Permission errors
- Verify role IDs in configuration
- Check user has the required role
- Make sure the bot can see the roles

### Cannot connect to game servers
- Verify server URLs and ports in `servers.xml`
- Check API key is correct
- Ensure game servers are running and accessible

### Build errors
- Make sure Java 11+ is installed
- Run `mvn clean install` to refresh dependencies

## Development

### Adding new commands

1. Create a new class implementing `Command` interface
2. Implement `getCommandData()`, `execute()`, and `getRequiredPermission()`
3. Register the command in `CommandManager.registerCommands()`

Example:
```java
public class MyCommand implements Command {
    @Override
    public CommandData getCommandData() {
        return Commands.slash("mycommand", "Description")
                .addOption(OptionType.STRING, "param", "Parameter", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Command logic here
    }

    @Override
    public PermissionLevel getRequiredPermission() {
        return PermissionLevel.MODERATOR;
    }
}
```

### Testing locally

For faster command updates during development, use guild-specific commands:
```java
// In CommandCenterBot.java, replace:
jda.updateCommands().addCommands(commandDataList).queue();

// With:
jda.getGuildById("YOUR_GUILD_ID")
   .updateCommands()
   .addCommands(commandDataList)
   .queue();
```

## License

This project is private and intended for internal use only.

## Support

For issues or questions, contact the development team.
