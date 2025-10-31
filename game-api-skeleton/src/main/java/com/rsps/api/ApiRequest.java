package com.rsps.api;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Wrapper for API request data
 */
public class ApiRequest {

    private final JSONObject body;
    private final String authenticatedUsername;

    public ApiRequest(JSONObject body, String authenticatedUsername) {
        this.body = body;
        this.authenticatedUsername = authenticatedUsername;
    }

    /**
     * Parse request from input stream
     */
    public static ApiRequest parse(InputStream inputStream, String authenticatedUsername) throws IOException, ParseException {
        String bodyText = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));

        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(bodyText);

        return new ApiRequest(json, authenticatedUsername);
    }

    /**
     * Get string parameter from request body
     */
    public String getString(String key) {
        return (String) body.get(key);
    }

    /**
     * Get integer parameter from request body
     * Handles both Long and Integer types from JSON
     */
    public Integer getInt(String key) {
        Object obj = body.get(key);
        if (obj == null) {
            return null;
        }
        if (obj instanceof Long) {
            return ((Long) obj).intValue();
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        throw new IllegalArgumentException("Parameter '" + key + "' is not an integer");
    }

    /**
     * Get boolean parameter from request body
     */
    public Boolean getBoolean(String key) {
        Object obj = body.get(key);
        if (obj == null) {
            return null;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        throw new IllegalArgumentException("Parameter '" + key + "' is not a boolean");
    }

    /**
     * Get the full JSON body
     */
    public JSONObject getBody() {
        return body;
    }

    /**
     * Get the username of the authenticated API user
     */
    public String getAuthenticatedUsername() {
        return authenticatedUsername;
    }
}
