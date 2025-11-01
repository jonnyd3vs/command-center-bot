package com.rsps.discordbot.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps Discord channel IDs to game servers
 */
public class ChannelMapper {

    private static final Map<String, ServerConfig> channelToServerMap = new HashMap<>();

    /**
     * Initialize the channel mapper with server configurations
     * This should be called at bot startup with the loaded servers
     *
     * @param servers List of server configurations with testing mode already applied
     */
    public static void initialize(List<ServerConfig> servers) {
        channelToServerMap.clear();

        for (ServerConfig server : servers) {
            if (server.getChannelId() != null && !server.getChannelId().isEmpty()
                && !server.getChannelId().equals("YOUR_DISCORD_CHANNEL_ID")) {
                channelToServerMap.put(server.getChannelId(), server);
                System.out.println("[ChannelMapper] Loaded channel " + server.getChannelId() + " for server " + server.getName());
            }
        }

        System.out.println("[ChannelMapper] Initialized with " + channelToServerMap.size() + " channel mappings");
    }

    /**
     * Get server configuration for a given channel ID
     *
     * @param channelId The Discord channel ID
     * @return ServerConfig if channel is mapped, null otherwise
     */
    public static ServerConfig getServerForChannel(String channelId) {
        return channelToServerMap.get(channelId);
    }

    /**
     * Check if a channel is mapped to a server
     *
     * @param channelId The Discord channel ID
     * @return true if channel has a server mapping
     */
    public static boolean isChannelMapped(String channelId) {
        return channelToServerMap.containsKey(channelId);
    }

    /**
     * Get all mapped channel IDs
     *
     * @return Set of channel IDs that are mapped
     */
    public static java.util.Set<String> getMappedChannels() {
        return channelToServerMap.keySet();
    }
}
