package com.rsps.discordbot.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps Discord channel IDs to game servers
 */
public class ChannelMapper {

    private static final Map<String, ServerConfig> channelToServerMap = new HashMap<>();

    static {
        // Fantasy Server - Channel ID: 1433696512814354522
        ServerConfig fantasy = new ServerConfig(
            "Fantasy",
            "54.39.68.229",
            8049,
            "1433696512814354522"
        );
        channelToServerMap.put("1433696512814354522", fantasy);

        // Vale and Azerite will be added later
        // Vale Server - Channel ID: 1433696608679362650
        // channelToServerMap.put("1433696608679362650", vale);

        // Azerite Server - Channel ID: 1433696582288674836
        // channelToServerMap.put("1433696582288674836", azerite);
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
