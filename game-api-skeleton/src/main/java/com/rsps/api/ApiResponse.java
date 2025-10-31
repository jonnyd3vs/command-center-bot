package com.rsps.api;

import org.json.simple.JSONObject;

/**
 * Standard API response wrapper
 */
public class ApiResponse {

    private final boolean success;
    private final String message;
    private final JSONObject data;

    private ApiResponse(boolean success, String message, JSONObject data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static ApiResponse success(String message) {
        return new ApiResponse(true, message, null);
    }

    public static ApiResponse success(String message, JSONObject data) {
        return new ApiResponse(true, message, data);
    }

    public static ApiResponse error(String message) {
        return new ApiResponse(false, message, null);
    }

    @SuppressWarnings("unchecked")
    public String toJson() {
        JSONObject json = new JSONObject();
        json.put("success", success);
        json.put("message", message);
        if (data != null) {
            json.put("data", data);
        }
        return json.toJSONString();
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public JSONObject getData() {
        return data;
    }
}
