package com.rsps.api.handlers.examples;

import com.rsps.api.ApiEndpoint;
import com.rsps.api.ApiRequest;
import com.rsps.api.ApiResponse;
import com.rsps.api.GameApiHandler;

/**
 * Example handler for /clear-progress endpoint
 *
 * This handler resets a player's progress (equivalent to ::progress command).
 *
 * Expected request body:
 * {
 *   "username": "string"
 * }
 */
@ApiEndpoint("/clear-progress")
public class ClearProgressHandler implements GameApiHandler {

    @Override
    public ApiResponse handle(ApiRequest request) {
        String username = request.getString("username");

        if (username == null || username.trim().isEmpty()) {
            return ApiResponse.error("Username is required");
        }

        try {
            // TODO: Implement your progress reset logic here
            // Example implementation:
            // PlayerProfile profile = YourGameDatabase.getPlayerProfile(username);
            // if (profile == null) {
            //     return ApiResponse.error("Player not found");
            // }
            //
            // profile.resetProgress();
            // profile.save();
            //
            // // If player is online, update their client
            // Player player = YourGameWorld.getPlayer(username);
            // if (player != null) {
            //     player.refresh();
            //     player.sendMessage("Your progress has been reset by a Discord administrator.");
            // }

            return ApiResponse.success("Progress cleared for " + username);

        } catch (Exception e) {
            return ApiResponse.error("Failed to clear progress: " + e.getMessage());
        }
    }
}
