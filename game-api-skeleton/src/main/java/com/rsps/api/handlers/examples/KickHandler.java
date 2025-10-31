package com.rsps.api.handlers.examples;

import com.rsps.api.ApiEndpoint;
import com.rsps.api.ApiRequest;
import com.rsps.api.ApiResponse;
import com.rsps.api.GameApiHandler;

/**
 * Example handler for /kick endpoint
 *
 * This handler kicks a player offline.
 *
 * Expected request body:
 * {
 *   "username": "string"
 * }
 */
@ApiEndpoint("/kick")
public class KickHandler implements GameApiHandler {

    @Override
    public ApiResponse handle(ApiRequest request) {
        String username = request.getString("username");

        if (username == null || username.trim().isEmpty()) {
            return ApiResponse.error("Username is required");
        }

        try {
            // TODO: Implement your kick logic here
            // Example implementation:
            // Player player = YourGameWorld.getPlayer(username);
            // if (player == null) {
            //     return ApiResponse.error("Player not found or offline");
            // }
            //
            // player.disconnect("You have been kicked by a Discord administrator.");

            return ApiResponse.success("Player " + username + " has been kicked");

        } catch (Exception e) {
            return ApiResponse.error("Failed to kick player: " + e.getMessage());
        }
    }
}
