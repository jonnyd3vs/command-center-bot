package com.rsps.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Main API server class that handles HTTP requests and authentication
 *
 * This is the skeleton that game servers will use to quickly set up an API
 */
public class GameApiServer {

    private final ApiConfig config;
    private final Map<String, GameApiHandler> endpoints = new HashMap<>();
    private HttpServer server;

    public GameApiServer(ApiConfig config) {
        this.config = config;
    }

    /**
     * Register an endpoint with a handler
     *
     * @param path The endpoint path (e.g., "/give-item")
     * @param handler The handler implementation
     */
    public void registerEndpoint(String path, GameApiHandler handler) {
        endpoints.put(path, handler);
    }

    /**
     * Auto-register all handlers in a package using reflection
     * Scans for classes with @ApiEndpoint annotation
     *
     * @param packageName The package to scan (e.g., "com.realm.api.skeleton")
     */
    public void registerHandlersInPackage(String packageName) {
        try {
            // Get all classes in the package
            Class<?>[] classes = getClassesInPackage(packageName);

            for (Class<?> clazz : classes) {
                // Check if class has @ApiEndpoint annotation
                if (clazz.isAnnotationPresent(ApiEndpoint.class)) {
                    ApiEndpoint annotation = clazz.getAnnotation(ApiEndpoint.class);
                    String path = annotation.value();

                    // Check if class implements GameApiHandler
                    if (GameApiHandler.class.isAssignableFrom(clazz)) {
                        try {
                            // Instantiate the handler
                            GameApiHandler handler = (GameApiHandler) clazz.newInstance();
                            registerEndpoint(path, handler);
                            System.out.println("[API Server] Auto-registered: " + path + " -> " + clazz.getSimpleName());
                        } catch (Exception e) {
                            System.err.println("[API Server] Failed to instantiate handler: " + clazz.getName());
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[API Server] Failed to auto-register handlers in package: " + packageName);
            e.printStackTrace();
        }
    }

    /**
     * Get all classes in a package using reflection
     * Java 8 compatible implementation
     */
    private Class<?>[] getClassesInPackage(String packageName) {
        String path = packageName.replace('.', '/');
        java.util.ArrayList<Class<?>> classes = new java.util.ArrayList<Class<?>>();

        try {
            java.net.URL[] urls = ((java.net.URLClassLoader) Thread.currentThread().getContextClassLoader()).getURLs();

            for (java.net.URL url : urls) {
                java.io.File directory = new java.io.File(url.getFile());

                if (directory.exists()) {
                    scanDirectory(directory, packageName, classes);
                } else {
                    // Try as JAR file
                    scanJar(url.getFile(), path, packageName, classes);
                }
            }
        } catch (Exception e) {
            System.err.println("[API Server] Error scanning package: " + e.getMessage());
        }

        return classes.toArray(new Class<?>[0]);
    }

    /**
     * Scan directory for classes
     */
    private void scanDirectory(java.io.File directory, String packageName, java.util.List<Class<?>> classes) {
        String packagePath = packageName.replace('.', java.io.File.separatorChar);
        String fullPath = directory.getAbsolutePath();

        if (fullPath.contains(packagePath)) {
            // Find the base directory
            int idx = fullPath.indexOf(packagePath);
            String basePath = fullPath.substring(0, idx);

            scanDirectoryRecursive(new java.io.File(fullPath), basePath, packageName, classes);
        } else {
            // Scan subdirectories
            java.io.File packageDir = new java.io.File(directory, packagePath);
            if (packageDir.exists()) {
                scanDirectoryRecursive(packageDir, directory.getAbsolutePath() + java.io.File.separator, packageName, classes);
            }
        }
    }

    /**
     * Recursively scan directory for .class files
     */
    private void scanDirectoryRecursive(java.io.File directory, String basePath, String packageName, java.util.List<Class<?>> classes) {
        java.io.File[] files = directory.listFiles();
        if (files == null) return;

        for (java.io.File file : files) {
            if (file.isDirectory()) {
                String subPackage = packageName + "." + file.getName();
                scanDirectoryRecursive(file, basePath, subPackage, classes);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                } catch (ClassNotFoundException e) {
                    // Skip classes that can't be loaded
                }
            }
        }
    }

    /**
     * Scan JAR file for classes
     */
    private void scanJar(String jarPath, String packagePath, String packageName, java.util.List<Class<?>> classes) {
        try {
            if (jarPath.startsWith("file:")) {
                jarPath = jarPath.substring(5);
            }
            int jarEnd = jarPath.indexOf("!");
            if (jarEnd > 0) {
                jarPath = jarPath.substring(0, jarEnd);
            }

            java.io.File jarFile = new java.io.File(jarPath);
            if (!jarFile.exists() || !jarFile.getName().endsWith(".jar")) {
                return;
            }

            java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile);
            java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                java.util.jar.JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.startsWith(packagePath) && name.endsWith(".class")) {
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    if (className.startsWith(packageName)) {
                        try {
                            Class<?> clazz = Class.forName(className);
                            classes.add(clazz);
                        } catch (ClassNotFoundException | NoClassDefFoundError e) {
                            // Skip classes that can't be loaded
                        }
                    }
                }
            }
            jar.close();
        } catch (Exception e) {
            // Skip JARs that can't be read
        }
    }

    /**
     * Start the API server
     *
     * @return true if started successfully, false otherwise
     */
    public boolean start() {
        try {
            System.out.println("[API Server] Starting on port " + config.getPort() + "...");

            // Create HTTP server
            server = HttpServer.create(new InetSocketAddress(config.getPort()), 0);

            // Register all endpoints
            for (Map.Entry<String, GameApiHandler> entry : endpoints.entrySet()) {
                String path = entry.getKey();
                GameApiHandler handler = entry.getValue();

                // Wrap handler with authentication
                HttpHandler httpHandler = createAuthenticatedHandler(handler);
                server.createContext(path, httpHandler);

                System.out.println("[API Server]   Registered: " + handler.getMethod() + " " + path);
            }

            // Set thread pool
            server.setExecutor(Executors.newFixedThreadPool(config.getThreadPoolSize()));

            // Start server
            server.start();

            System.out.println("[API Server] Started successfully!");
            System.out.println("[API Server] API authentication enabled");

            return true;

        } catch (IOException e) {
            System.err.println("[API Server] FAILED TO START - Port " + config.getPort() + " may already be in use");
            System.err.println("[API Server] Error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("[API Server] FAILED TO START - Unexpected error");
            System.err.println("[API Server] Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Stop the API server
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[API Server] Stopped");
        }
    }

    /**
     * Create an authenticated HTTP handler that wraps the game handler
     */
    private HttpHandler createAuthenticatedHandler(GameApiHandler gameHandler) {
        return exchange -> {
            try {
                // Check HTTP method
                if (!exchange.getRequestMethod().equalsIgnoreCase(gameHandler.getMethod())) {
                    sendResponse(exchange, 405, ApiResponse.error("Method not allowed. Use " + gameHandler.getMethod() + "."));
                    return;
                }

                // Check for API key in Authorization header
                String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    sendResponse(exchange, 401, ApiResponse.error("Unauthorized: Missing or invalid Authorization header"));
                    return;
                }

                // Extract and validate API key
                String providedKey = authHeader.substring(7); // Remove "Bearer " prefix

                if (!providedKey.equals(config.getApiKey())) {
                    sendResponse(exchange, 401, ApiResponse.error("Unauthorized: Invalid API key"));
                    return;
                }

                // Parse request (authenticated as DiscordBot)
                ApiRequest request = ApiRequest.parse(exchange.getRequestBody(), "DiscordBot");

                // Call game handler
                ApiResponse response = gameHandler.handle(request);

                // Send response
                int statusCode = response.isSuccess() ? 200 : 400;
                sendResponse(exchange, statusCode, response);

            } catch (org.json.simple.parser.ParseException e) {
                sendResponse(exchange, 400, ApiResponse.error("Invalid JSON in request body"));
            } catch (IllegalArgumentException e) {
                sendResponse(exchange, 400, ApiResponse.error("Bad request: " + e.getMessage()));
            } catch (Exception e) {
                System.err.println("[API Server] Error handling request: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, ApiResponse.error("Internal server error: " + e.getMessage()));
            }
        };
    }

    /**
     * Send HTTP response
     */
    private void sendResponse(HttpExchange exchange, int statusCode, ApiResponse response) throws IOException {
        String responseJson = response.toJson();
        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
