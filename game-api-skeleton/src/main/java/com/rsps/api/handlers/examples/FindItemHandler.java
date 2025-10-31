package com.rsps.api.handlers.examples;

import com.rsps.api.ApiEndpoint;
import com.rsps.api.ApiRequest;
import com.rsps.api.ApiResponse;
import com.rsps.api.GameApiHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Example handler for /find-item endpoint
 *
 * This handler searches for items by name and returns up to 50 matching results.
 *
 * Expected request body:
 * {
 *   "itemName": "string"
 * }
 *
 * Response data format:
 * {
 *   "items": [
 *     {"id": 123, "name": "Item Name"},
 *     ...
 *   ]
 * }
 */
@ApiEndpoint("/find-item")
public class FindItemHandler implements GameApiHandler {

    @Override
    @SuppressWarnings("unchecked")
    public ApiResponse handle(ApiRequest request) {
        String itemName = request.getString("itemName");

        if (itemName == null || itemName.trim().isEmpty()) {
            return ApiResponse.error("Item name is required");
        }

        try {
            // TODO: Implement your item search logic here
            // Example implementation:
            JSONArray items = new JSONArray();

            // Search your game's item database
            // List<GameItem> matchingItems = YourGameItemDatabase.search(itemName, 50);
            // for (GameItem gameItem : matchingItems) {
            //     JSONObject item = new JSONObject();
            //     item.put("id", gameItem.getId());
            //     item.put("name", gameItem.getName());
            //     items.add(item);
            // }

            // Placeholder response (remove when implementing)
            JSONObject sampleItem = new JSONObject();
            sampleItem.put("id", 995);
            sampleItem.put("name", "Coins");
            items.add(sampleItem);

            JSONObject data = new JSONObject();
            data.put("items", items);

            return ApiResponse.success("Found " + items.size() + " item(s)", data);

        } catch (Exception e) {
            return ApiResponse.error("Failed to search items: " + e.getMessage());
        }
    }
}
