package com.rsps.api;

/**
 * Configuration for the API server
 */
public class ApiConfig {

    private final int port;
    private final String apiKey; // Single API key for authentication
    private final int threadPoolSize;

    private ApiConfig(Builder builder) {
        this.port = builder.port;
        this.apiKey = builder.apiKey;
        this.threadPoolSize = builder.threadPoolSize;
    }

    public int getPort() {
        return port;
    }

    public String getApiKey() {
        return apiKey;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public static class Builder {
        private int port = 8090;
        private String apiKey = null;
        private int threadPoolSize = 10;

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder threadPoolSize(int size) {
            this.threadPoolSize = size;
            return this;
        }

        public ApiConfig build() {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalStateException("API key must be configured");
            }
            return new ApiConfig(this);
        }
    }
}
