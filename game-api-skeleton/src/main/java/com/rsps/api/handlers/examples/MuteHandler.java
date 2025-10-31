package com.rsps.api.handlers.examples;

import com.rsps.api.ApiEndpoint;
import com.rsps.api.ApiRequest;
import com.rsps.api.ApiResponse;
import com.rsps.api.GameApiHandler;

/**
 * Example handler for /mute endpoint
 *
 * This handler mutes a player in the game.
 *
 * Expected request body:
 * {
 *   "username": "string"
 * }
 */
@ApiEndpoint("/mute")
public class MuteHandler implements GameApiHandler {

    @Override
    public ApiResponse handle(ApiRequest request) {
        String username = request.getString("username");

        if (username == null || username.trim().isEmpty()) {
            return ApiResponse.error("Username is required");
        }

        try {
            // TODO: Implement your mute logic here
            // Example implementation:
            // Player player = YourGameWorld.getPlayer(username);
            // if (player == null) {
            //     return ApiResponse.error("Player not found or offline");
            // }
            //
            // player.setMuted(true);
            // player.sendMessage("You have been muted by a Discord administrator.");

            return ApiResponse.success("Player " + username + " has been muted");

        } catch (Exception e) {
            return ApiResponse.error("Failed to mute player: " + e.getMessage());
        }
    }
}
