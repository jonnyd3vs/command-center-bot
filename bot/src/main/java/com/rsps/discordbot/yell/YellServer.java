package com.rsps.discordbot.yell;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rsps.discordbot.config.BotConfig;
import com.rsps.discordbot.config.ServerConfig;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * HTTP server that receives yell messages from game servers
 */
public class YellServer {

    private final BotConfig botConfig;
    private final List<ServerConfig> servers;
    private final JDA jda;
    private final YellMessageQueue messageQueue;
    private final Gson gson;
    private HttpServer server;
    private final int port;

    public YellServer(BotConfig botConfig, List<ServerConfig> servers, JDA jda, int port) {
        this.botConfig = botConfig;
        this.servers = servers;
        this.jda = jda;
        this.port = port;
        this.messageQueue = new YellMessageQueue(jda);
        this.gson = new Gson();
    }

    /**
     * Start the HTTP server
     */
    public void start() {
        try {
            System.out.println("[Yell Server] Starting on port " + port + "...");

            server = HttpServer.create(new InetSocketAddress(port), 0);

            // Register yell endpoint
            server.createContext("/yell", new YellHandler());

            server.setExecutor(Executors.newFixedThreadPool(4));
            server.start();

            // Start message queue processor
            messageQueue.start();

            System.out.println("[Yell Server] Started successfully on port " + port);

        } catch (IOException e) {
            System.err.println("[Yell Server] FAILED TO START - Port " + port + " may already be in use");
            System.err.println("[Yell Server] Error: " + e.getMessage());
        }
    }

    /**
     * Stop the HTTP server
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[Yell Server] Stopped");
        }
        if (messageQueue != null) {
            messageQueue.stop();
        }
    }

    /**
     * Get the message queue
     */
    public YellMessageQueue getMessageQueue() {
        return messageQueue;
    }

    /**
     * HTTP handler for yell endpoint
     */
    private class YellHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\":\"Method not allowed. Use POST.\"}");
                return;
            }

            try {
                // Check API key authorization
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    sendResponse(exchange, 401, "{\"error\":\"Unauthorized: Missing or invalid Authorization header\"}");
                    return;
                }

                String apiKey = authHeader.substring(7); // Remove "Bearer " prefix

                // Validate API key and find corresponding server
                ServerConfig serverConfig = findServerByApiKey(apiKey);
                if (serverConfig == null) {
                    sendResponse(exchange, 401, "{\"error\":\"Unauthorized: Invalid API key\"}");
                    return;
                }

                // Parse request body
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = gson.fromJson(body, JsonObject.class);

                String message = json.has("message") ? json.get("message").getAsString() : null;
                String playerName = json.has("playerName") ? json.get("playerName").getAsString() : null;
                String serverName = json.has("serverName") ? json.get("serverName").getAsString() : null;

                if (message == null || message.trim().isEmpty()) {
                    sendResponse(exchange, 400, "{\"error\":\"Message is required\"}");
                    return;
                }

                // Find the correct server config by name if provided
                if (serverName != null && !serverName.trim().isEmpty()) {
                    serverConfig = findServerByName(serverName);
                    if (serverConfig == null) {
                        sendResponse(exchange, 400, "{\"error\":\"Server not found: " + serverName + "\"}");
                        return;
                    }
                }

                // Get the channel for this server
                String channelId = serverConfig.getChannelId();
                TextChannel channel = jda.getTextChannelById(channelId);

                if (channel == null) {
                    sendResponse(exchange, 500, "{\"error\":\"Discord channel not found for server: " + serverConfig.getName() + "\"}");
                    return;
                }

                // Format the message
                String formattedMessage = formatYellMessage(serverConfig.getName(), playerName, message);

                // Queue the message
                messageQueue.queueMessage(channel, formattedMessage);

                sendResponse(exchange, 200, "{\"success\":true,\"message\":\"Yell message queued\"}");

            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"Internal server error: " + e.getMessage() + "\"}");
            }
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    /**
     * Find server by API key
     */
    private ServerConfig findServerByApiKey(String apiKey) {
        // For now, we use the same API key for all servers
        // and rely on the server configuration to match
        String configuredApiKey = botConfig.getApiKey();
        if (configuredApiKey != null && configuredApiKey.equals(apiKey)) {
            // Return the first server or implement multi-key support
            // For multi-server setups, you might want different API keys per server
            return servers.isEmpty() ? null : servers.get(0);
        }
        return null;
    }

    /**
     * Find server by name
     */
    private ServerConfig findServerByName(String name) {
        for (ServerConfig server : servers) {
            if (server.getName().equalsIgnoreCase(name)) {
                return server;
            }
        }
        return null;
    }

    /**
     * Format yell message for Discord
     */
    private String formatYellMessage(String serverName, String playerName, String message) {
        if (playerName != null && !playerName.trim().isEmpty()) {
            return "**[" + serverName + "]** " + playerName + ": " + message;
        } else {
            return "**[" + serverName + "]** " + message;
        }
    }
}
