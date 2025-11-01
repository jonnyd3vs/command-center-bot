package com.rsps.discordbot.stats;

import com.rsps.discordbot.client.GameServerClient;
import com.rsps.discordbot.config.BotConfig;
import com.rsps.discordbot.config.ServerConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.awt.Color;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Polls game server statistics every 5 seconds and updates Discord messages
 */
public class GameStatsPoller {

    private final JDA jda;
    private final BotConfig botConfig;
    private final List<ServerConfig> servers;  // Servers list with testing mode already applied
    private final ScheduledExecutorService scheduler;
    private final Map<String, String> statsMessageIds;  // Map of server name to message ID

    public GameStatsPoller(JDA jda, BotConfig botConfig, List<ServerConfig> servers) {
        this.jda = jda;
        this.botConfig = botConfig;
        this.servers = servers;
        this.scheduler = Executors.newScheduledThreadPool(1);
        this.statsMessageIds = new HashMap<>();
    }

    /**
     * Start the stats polling service
     */
    public void start() {
        // Clear old messages from stats channels on startup
        clearOldStatsMessages();

        // Poll every 5 seconds
        scheduler.scheduleAtFixedRate(this::updateAllServerStats, 0, 5, TimeUnit.SECONDS);
        System.out.println("GameStatsPoller started - updating stats every 5 seconds");
    }

    /**
     * Clear old stats messages from all configured stats channels
     */
    private void clearOldStatsMessages() {
        for (ServerConfig server : servers) {
            if (server.getStatsChannelId() != null && !server.getStatsChannelId().trim().isEmpty()) {
                TextChannel statsChannel = jda.getTextChannelById(server.getStatsChannelId());
                if (statsChannel != null) {
                    try {
                        System.out.println("[Stats] Clearing old messages from stats channel for " + server.getName());
                        statsChannel.getIterableHistory().complete().forEach(message -> {
                            try {
                                message.delete().complete();
                            } catch (Exception e) {
                                System.err.println("[Stats] Failed to delete message: " + e.getMessage());
                            }
                        });
                        System.out.println("[Stats] Cleared old stats messages for " + server.getName());
                    } catch (Exception e) {
                        System.err.println("[Stats] Error clearing old messages: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Stop the stats polling service
     */
    public void stop() {
        scheduler.shutdown();
        System.out.println("GameStatsPoller stopped");
    }

    /**
     * Update stats for all configured servers
     */
    private void updateAllServerStats() {
        for (ServerConfig server : servers) {
            // Only update stats for servers with a stats channel configured
            if (server.getStatsChannelId() != null && !server.getStatsChannelId().trim().isEmpty()) {
                updateServerStats(server);
            }
        }
    }

    /**
     * Update stats for a specific server
     *
     * @param server The server configuration
     */
    private void updateServerStats(ServerConfig server) {
        // Get the stats channel
        TextChannel statsChannel = jda.getTextChannelById(server.getStatsChannelId());
        if (statsChannel == null) {
            System.err.println("Stats channel not found for server: " + server.getName());
            return;
        }

        EmbedBuilder embed;

        try {
            // Create client and fetch stats
            GameServerClient client = new GameServerClient(server.getUrl(), botConfig.getApiKey());
            Map<String, Object> stats = client.getStats();
            client.close();

            // Build the stats embed
            embed = buildStatsEmbed(server.getName(), stats);

        } catch (Exception e) {
            // Server is offline or unreachable, show offline status
            System.err.println("Server " + server.getName() + " is offline or unreachable: " + e.getMessage());
            embed = buildOfflineEmbed(server.getName());
        }

        // Make embed final for use in lambdas
        final EmbedBuilder finalEmbed = embed;

        // Check if we have an existing message to edit
        String existingMessageId = statsMessageIds.get(server.getName());

        if (existingMessageId != null) {
            // Try to edit existing message
            try {
                statsChannel.editMessageEmbedsById(existingMessageId, finalEmbed.build()).queue(
                    success -> {
                        // Message edited successfully
                    },
                    error -> {
                        // Message not found or error, create a new one
                        createNewStatsMessage(statsChannel, server.getName(), finalEmbed);
                    }
                );
            } catch (Exception e) {
                // Error editing, create new message
                createNewStatsMessage(statsChannel, server.getName(), finalEmbed);
            }
        } else {
            // No existing message, create a new one
            createNewStatsMessage(statsChannel, server.getName(), finalEmbed);
        }
    }

    /**
     * Create a new stats message in the channel
     *
     * @param channel The channel to send the message to
     * @param serverName The name of the server
     * @param embed The embed to send
     */
    private void createNewStatsMessage(TextChannel channel, String serverName, EmbedBuilder embed) {
        channel.sendMessageEmbeds(embed.build()).queue(
            message -> {
                // Store the message ID for future edits
                statsMessageIds.put(serverName, message.getId());
            },
            error -> {
                System.err.println("Error creating stats message for " + serverName + ": " + error.getMessage());
            }
        );
    }

    /**
     * Build a Discord embed with server statistics
     *
     * @param serverName The name of the server
     * @param stats The stats data from the server
     * @return EmbedBuilder with formatted stats
     */
    private EmbedBuilder buildStatsEmbed(String serverName, Map<String, Object> stats) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle(serverName + " - Server Statistics");
        embed.setColor(Color.CYAN);
        embed.setTimestamp(Instant.now());

        // Extract stats with safe casting
        int playersOnline = getIntStat(stats, "playersOnline");
        int aiOnline = getIntStat(stats, "aiOnline");
        int setpAmount = getIntStat(stats, "setpAmount");
        int uniqueIPs = getIntStat(stats, "uniqueIPs");
        int playersAtHome = getIntStat(stats, "playersAtHome");
        boolean released = getBooleanStat(stats, "released");
        String uptime = getStringStat(stats, "uptime");
        int updateTimer = getIntStat(stats, "updateTimer");

        // Add fields
        embed.addField("Players Online", String.valueOf(playersOnline), true);
        embed.addField("AI Online", String.valueOf(aiOnline), true);
        embed.addField("Fake Players (::setp)", String.valueOf(setpAmount), true);

        embed.addField("Unique IPs Online", String.valueOf(uniqueIPs), true);
        embed.addField("Players At ::Home", String.valueOf(playersAtHome), true);
        embed.addField("Server ::Released", released ? "true" : "false", true);

        embed.addField("Uptime", uptime != null ? uptime : "Unknown", true);

        if (updateTimer > 0) {
            embed.addField("Update Timer", updateTimer + " ticks", true);
            embed.setFooter("Server will restart in " + updateTimer + " ticks");
        } else {
            embed.addField("Update Timer", "Not active", true);
        }

        return embed;
    }

    /**
     * Build a Discord embed showing server offline status
     *
     * @param serverName The name of the server
     * @return EmbedBuilder with offline status
     */
    private EmbedBuilder buildOfflineEmbed(String serverName) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("Server Offline - " + serverName);
        embed.setDescription("The server is currently offline or unreachable.");
        embed.setColor(Color.RED);
        embed.setTimestamp(Instant.now());

        return embed;
    }

    /**
     * Safely extract an integer stat from the stats map
     */
    private int getIntStat(Map<String, Object> stats, String key) {
        Object value = stats.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    /**
     * Safely extract a boolean stat from the stats map
     */
    private boolean getBooleanStat(Map<String, Object> stats, String key) {
        Object value = stats.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return false;
    }

    /**
     * Safely extract a string stat from the stats map
     */
    private String getStringStat(Map<String, Object> stats, String key) {
        Object value = stats.get(key);
        if (value != null) {
            return value.toString();
        }
        return null;
    }
}
