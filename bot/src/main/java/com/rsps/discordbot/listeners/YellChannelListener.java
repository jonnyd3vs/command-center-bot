package com.rsps.discordbot.listeners;

import com.google.gson.Gson;
import com.rsps.discordbot.client.GameServerClient;
import com.rsps.discordbot.config.BotConfig;
import com.rsps.discordbot.config.ServerConfig;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Listener for messages in yell channels
 * Sends Discord messages back to the game server to appear as in-game yells
 */
public class YellChannelListener extends ListenerAdapter {

    private static final int MAX_MESSAGE_LENGTH = 300;

    private final BotConfig botConfig;
    private final List<ServerConfig> servers;
    private final Map<String, GameServerClient> serverClients;
    private final Gson gson;

    public YellChannelListener(BotConfig botConfig, List<ServerConfig> servers) {
        this.botConfig = botConfig;
        this.servers = servers;
        this.serverClients = new HashMap<>();
        this.gson = new Gson();

        // Initialize game server clients for each server
        for (ServerConfig server : servers) {
            if (server.getYellChannelId() != null && !server.getYellChannelId().isEmpty()) {
                GameServerClient client = new GameServerClient(server.getUrl(), botConfig.getApiKey());
                serverClients.put(server.getYellChannelId(), client);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignore bot messages
        if (event.getAuthor().isBot()) {
            return;
        }

        // Check if this is a yell channel
        String channelId = event.getChannel().getId();
        GameServerClient client = serverClients.get(channelId);

        if (client == null) {
            return; // Not a yell channel we're monitoring
        }

        // Get message details
        // Use server nickname if available, otherwise fall back to username
        String discordUsername;
        if (event.getMember() != null) {
            discordUsername = event.getMember().getEffectiveName(); // Gets nickname or username
        } else {
            discordUsername = event.getAuthor().getName(); // Fallback for DMs
        }

        String messageContent = event.getMessage().getContentDisplay();

        // Ignore empty messages
        if (messageContent == null || messageContent.trim().isEmpty()) {
            return;
        }

        // Limit message to 300 characters (in-game constraint)
        if (messageContent.length() > MAX_MESSAGE_LENGTH) {
            messageContent = messageContent.substring(0, MAX_MESSAGE_LENGTH);
        }

        // Send to game server
        try {
            sendDiscordYellToGame(client, discordUsername, messageContent);
        } catch (Exception e) {
            System.err.println("[Yell Listener] Failed to send Discord yell to game server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send a Discord yell message to the game server
     */
    private void sendDiscordYellToGame(GameServerClient client, String discordUsername, String message) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("discordUsername", discordUsername);
            payload.put("message", message);

            // Send POST request to /discord-yell endpoint
            String jsonPayload = gson.toJson(payload);

            // We'll need to add this method to GameServerClient
            client.sendDiscordYell(discordUsername, message);

            System.out.println("[Yell Listener] Sent Discord yell from " + discordUsername + " to game server");
        } catch (Exception e) {
            throw new RuntimeException("Failed to send Discord yell to game server", e);
        }
    }
}
