package com.rsps.api;

/**
 * Interface that game servers must implement for each API endpoint
 *
 * Implement this interface to add game-specific logic for each command
 */
public interface GameApiHandler {

    /**
     * Handle the API request
     *
     * @param request The parsed request with body data and authenticated username
     * @return ApiResponse with success/error and any data
     */
    ApiResponse handle(ApiRequest request);

    /**
     * Get the HTTP method this handler accepts (POST, GET, etc.)
     * Default is POST
     */
    default String getMethod() {
        return "POST";
    }
}
