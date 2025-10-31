package com.rsps.api.handlers.examples;

import com.rsps.api.ApiEndpoint;
import com.rsps.api.ApiRequest;
import com.rsps.api.ApiResponse;
import com.rsps.api.GameApiHandler;
import org.json.simple.JSONObject;

/**
 * Example handler for /check-pin endpoint
 *
 * This handler checks a player's bank pin.
 *
 * Expected request body:
 * {
 *   "username": "string"
 * }
 *
 * Response data format:
 * {
 *   "username": "string",
 *   "pin": "1234" or null if no pin set
 * }
 */
@ApiEndpoint("/check-pin")
public class CheckPinHandler implements GameApiHandler {

    @Override
    @SuppressWarnings("unchecked")
    public ApiResponse handle(ApiRequest request) {
        String username = request.getString("username");

        if (username == null || username.trim().isEmpty()) {
            return ApiResponse.error("Username is required");
        }

        try {
            // TODO: Implement your pin check logic here
            // Example implementation:
            // PlayerProfile profile = YourGameDatabase.getPlayerProfile(username);
            // if (profile == null) {
            //     return ApiResponse.error("Player not found");
            // }
            //
            // String pin = profile.getBankPin();

            JSONObject data = new JSONObject();
            data.put("username", username);
            data.put("pin", null); // TODO: Replace with actual pin or null if not set

            return ApiResponse.success("Retrieved pin for " + username, data);

        } catch (Exception e) {
            return ApiResponse.error("Failed to check pin: " + e.getMessage());
        }
    }
}
