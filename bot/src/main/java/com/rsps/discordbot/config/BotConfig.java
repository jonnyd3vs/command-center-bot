package com.rsps.discordbot.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Bot configuration loader
 * Loads configuration from bot.properties file or environment variables
 */
public class BotConfig {

    private static final String CONFIG_FILE = "bot.properties";
    private final Properties properties;

    public BotConfig() {
        this.properties = new Properties();
        loadConfig();
    }

    /**
     * Load configuration from file or environment variables
     */
    private void loadConfig() {
        // Try to load from file first
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                System.out.println("Loaded configuration from " + CONFIG_FILE);
            }
        } catch (IOException e) {
            System.out.println("Could not load " + CONFIG_FILE + ", will use environment variables");
        }

        // Try to load from external file (for production)
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
            System.out.println("Loaded configuration from external " + CONFIG_FILE);
        } catch (IOException e) {
            // File doesn't exist, that's okay
        }
    }

    /**
     * Get Discord bot token
     *
     * @return Bot token
     */
    public String getBotToken() {
        String token = properties.getProperty("discord.bot.token");
        if (token == null || token.trim().isEmpty()) {
            token = System.getenv("DISCORD_BOT_TOKEN");
        }
        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("Discord bot token not configured. Set discord.bot.token in bot.properties or DISCORD_BOT_TOKEN environment variable");
        }
        return token;
    }

    /**
     * Get API key for game server authentication
     *
     * @return API key
     */
    public String getApiKey() {
        String apiKey = properties.getProperty("api.key");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            apiKey = System.getenv("RSPS_API_KEY");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new RuntimeException("API key not configured. Set api.key in bot.properties or RSPS_API_KEY environment variable");
        }
        return apiKey;
    }

    /**
     * Get bot prefix for commands (optional, default: !)
     *
     * @return Command prefix
     */
    public String getCommandPrefix() {
        String prefix = properties.getProperty("command.prefix");
        if (prefix == null || prefix.trim().isEmpty()) {
            prefix = System.getenv("COMMAND_PREFIX");
        }
        if (prefix == null || prefix.trim().isEmpty()) {
            prefix = "!";
        }
        return prefix;
    }

    /**
     * Get admin role ID (optional)
     *
     * @return Admin role ID or null
     */
    public String getAdminRoleId() {
        String roleId = properties.getProperty("admin.role.id");
        if (roleId == null || roleId.trim().isEmpty()) {
            roleId = System.getenv("ADMIN_ROLE_ID");
        }
        return roleId;
    }

    /**
     * Get moderator role ID (optional)
     *
     * @return Moderator role ID or null
     */
    public String getModeratorRoleId() {
        String roleId = properties.getProperty("moderator.role.id");
        if (roleId == null || roleId.trim().isEmpty()) {
            roleId = System.getenv("MODERATOR_ROLE_ID");
        }
        return roleId;
    }

    /**
     * Get yell server port (optional, default: 8091)
     *
     * @return Yell server port
     */
    public int getYellServerPort() {
        String portStr = properties.getProperty("yell.server.port");
        if (portStr == null || portStr.trim().isEmpty()) {
            portStr = System.getenv("YELL_SERVER_PORT");
        }
        if (portStr == null || portStr.trim().isEmpty()) {
            return 8091; // Default port
        }
        try {
            return Integer.parseInt(portStr.trim());
        } catch (NumberFormatException e) {
            System.err.println("Invalid yell server port, using default 8091");
            return 8091;
        }
    }
}
