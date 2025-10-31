package com.rsps.discordbot.client;

import com.google.gson.Gson;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Client for communicating with the game server
 * Handles all POST requests to the server API with API key authentication
 */
public class GameServerClient {

    private final String serverUrl;
    private final String apiKey;
    private final CloseableHttpClient httpClient;
    private final Gson gson;

    public GameServerClient(String serverUrl, String apiKey) {
        this.serverUrl = serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
        this.apiKey = apiKey;
        this.httpClient = HttpClients.createDefault();
        this.gson = new Gson();
    }

    /**
     * Give an item to a player
     *
     * @param playerName The name of the player
     * @param itemId The ID of the item to give
     * @param amount The amount of the item
     * @throws IOException If the request fails
     */
    public void giveItem(String playerName, int itemId, int amount) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", playerName);
        payload.put("itemId", itemId);
        payload.put("amount", amount);

        sendPostRequest("/give-item", payload);
    }

    /**
     * Mass ban players
     *
     * @param playerName The name of the player to ban
     * @throws IOException If the request fails
     */
    public void massBan(String playerName) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", playerName);

        sendPostRequest("/mass-ban", payload);
    }

    /**
     * Give offers to a player
     *
     * @param playerName The name of the player
     * @param offerType The type of offer (not used in current API)
     * @param offerData Additional offer data (not used in current API)
     * @throws IOException If the request fails
     */
    public void giveOffers(String playerName, String offerType, String offerData) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", playerName);

        sendPostRequest("/give-offers", payload);
    }

    /**
     * Reset/set password for a player
     *
     * @param username The username
     * @return Map containing response data (username and newPassword)
     * @throws IOException If the request fails
     */
    public Map<String, Object> setPassword(String username) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);

        return sendPostRequestWithResponse("/set-password", payload);
    }

    /**
     * Find items by name
     *
     * @param itemName The name (or partial name) of the item to search for
     * @return Map containing list of matching items with their IDs
     * @throws IOException If the request fails
     */
    public Map<String, Object> findItem(String itemName) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("itemName", itemName);

        return sendPostRequestWithResponse("/find-item", payload);
    }

    /**
     * Mute a player
     *
     * @param username The username of the player to mute
     * @throws IOException If the request fails
     */
    public void mutePlayer(String username) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);

        sendPostRequest("/mute", payload);
    }

    /**
     * Check a player's bank pin
     *
     * @param username The username of the player
     * @return Map containing pin information
     * @throws IOException If the request fails
     */
    public Map<String, Object> checkPin(String username) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);

        return sendPostRequestWithResponse("/check-pin", payload);
    }

    /**
     * Clear a player's progress
     *
     * @param username The username of the player
     * @throws IOException If the request fails
     */
    public void clearProgress(String username) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);

        sendPostRequest("/clear-progress", payload);
    }

    /**
     * Kick a player offline
     *
     * @param username The username of the player to kick
     * @throws IOException If the request fails
     */
    public void kickPlayer(String username) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);

        sendPostRequest("/kick", payload);
    }

    /**
     * Send a message to players (legacy method)
     *
     * @param type The type of message (Global, Player, Announcement)
     * @param playerName The player name (if type is Player)
     * @param message The message text
     * @throws IOException If the request fails
     */
    public void sendMessage(String type, String playerName, String message) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "sendMessage");
        payload.put("messageType", type.toLowerCase());
        payload.put("message", message);

        if ("Player".equalsIgnoreCase(type)) {
            payload.put("playerName", playerName);
        }

        sendPostRequest("/api/admin/message", payload);
    }

    /**
     * Execute a server command (legacy method)
     *
     * @param command The command to execute
     * @param param Command parameter (e.g., player name)
     * @param customCommand Custom command string
     * @throws IOException If the request fails
     */
    public void executeCommand(String command, String param, String customCommand) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "executeCommand");

        if ("Custom Command".equals(command)) {
            payload.put("command", "custom");
            payload.put("customCommand", customCommand);
        } else {
            payload.put("command", command.toLowerCase().replace(" ", "_"));
            if (param != null && !param.isEmpty()) {
                payload.put("parameter", param);
            }
        }

        sendPostRequest("/api/admin/command", payload);
    }

    /**
     * Send a POST request to the server
     *
     * @param endpoint The API endpoint
     * @param payload The request payload
     * @throws IOException If the request fails
     */
    private void sendPostRequest(String endpoint, Map<String, Object> payload) throws IOException {
        sendPostRequestWithResponse(endpoint, payload);
    }

    /**
     * Send a POST request to the server and return response data
     *
     * @param endpoint The API endpoint
     * @param payload The request payload
     * @return Map containing response data
     * @throws IOException If the request fails
     */
    private Map<String, Object> sendPostRequestWithResponse(String endpoint, Map<String, Object> payload) throws IOException {
        String url = serverUrl + endpoint;
        HttpPost httpPost = new HttpPost(url);

        // Convert payload to JSON
        String jsonPayload = gson.toJson(payload);

        // Set the entity and content type
        StringEntity entity = new StringEntity(jsonPayload, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);

        // Set headers including API key
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("X-API-Key", apiKey);
        httpPost.setHeader("Authorization", "Bearer " + apiKey);

        // Execute the request
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            int statusCode = response.getCode();

            // Read response body
            String responseBody = "";
            if (response.getEntity() != null) {
                try {
                    responseBody = new String(response.getEntity().getContent().readAllBytes());
                } catch (Exception e) {
                    responseBody = "";
                }
            }

            if (statusCode < 200 || statusCode >= 300) {
                // Try to parse error message from response
                String errorMessage = parseErrorMessage(responseBody);
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    throw new IOException(errorMessage);
                } else {
                    throw new IOException("Server returned status code: " + statusCode);
                }
            }

            // Parse success response data
            return parseResponseData(responseBody);
        }
    }

    /**
     * Parse error message from server response
     *
     * @param responseBody The response body JSON
     * @return The error message or null
     */
    private String parseErrorMessage(String responseBody) {
        try {
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                // Try to parse as JSON and extract error message
                com.google.gson.JsonObject jsonResponse = gson.fromJson(responseBody, com.google.gson.JsonObject.class);

                // Try common error field names
                if (jsonResponse.has("error")) {
                    return jsonResponse.get("error").getAsString();
                } else if (jsonResponse.has("message")) {
                    return jsonResponse.get("message").getAsString();
                } else if (jsonResponse.has("errorMessage")) {
                    return jsonResponse.get("errorMessage").getAsString();
                }
            }
        } catch (Exception e) {
            // If parsing fails, return the raw response body
            return responseBody;
        }
        return null;
    }

    /**
     * Parse response data from server response
     *
     * @param responseBody The response body JSON
     * @return Map containing the response data
     */
    private Map<String, Object> parseResponseData(String responseBody) {
        try {
            if (responseBody != null && !responseBody.trim().isEmpty()) {
                com.google.gson.JsonObject jsonResponse = gson.fromJson(responseBody, com.google.gson.JsonObject.class);

                // Extract data field if it exists
                if (jsonResponse.has("data")) {
                    com.google.gson.JsonElement dataElement = jsonResponse.get("data");
                    if (dataElement.isJsonObject()) {
                        return gson.fromJson(dataElement, Map.class);
                    }
                }
            }
        } catch (Exception e) {
            // If parsing fails, return empty map
        }
        return new HashMap<>();
    }

    /**
     * Close the HTTP client
     */
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
