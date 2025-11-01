package com.rsps.discordbot.config;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a game server configuration
 */
public class ServerConfig {

    private String name;
    private String host;
    private int port;
    private String channelId;  // Discord channel ID for commands
    private String yellChannelId;  // Discord channel ID for yell messages
    private boolean testingMode;  // Whether to use localhost override

    public ServerConfig(String name, String host, int port, String channelId) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.channelId = channelId;
        this.yellChannelId = null;  // Optional
        this.testingMode = false;
    }

    public ServerConfig(String name, String host, int port, String channelId, String yellChannelId) {
        this.name = name;
        this.host = host;
        this.port = port;
        this.channelId = channelId;
        this.yellChannelId = yellChannelId;
        this.testingMode = false;
    }

    /**
     * Enable testing mode (uses localhost instead of configured host)
     */
    public void setTestingMode(boolean testingMode) {
        this.testingMode = testingMode;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        // In testing mode, override all hosts to localhost
        return testingMode ? "localhost" : host;
    }

    public int getPort() {
        return port;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getYellChannelId() {
        return yellChannelId;
    }

    public String getUrl() {
        // Use getHost() which respects testing mode
        return "http://" + getHost() + ":" + port;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Load server configurations from XML file in resources
     *
     * @return List of server configurations
     */
    public static List<ServerConfig> loadServerConfigs() {
        List<ServerConfig> servers = new ArrayList<>();

        try {
            // Load XML file from resources
            InputStream inputStream = ServerConfig.class.getResourceAsStream("/servers.xml");
            if (inputStream == null) {
                throw new RuntimeException("servers.xml not found in resources");
            }

            // Parse XML
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            doc.getDocumentElement().normalize();

            // Get all server elements
            NodeList nodeList = doc.getElementsByTagName("server");

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    String name = element.getElementsByTagName("name").item(0).getTextContent();
                    String host = element.getElementsByTagName("host").item(0).getTextContent();
                    int port = Integer.parseInt(element.getElementsByTagName("port").item(0).getTextContent());

                    // Channel ID is optional
                    String channelId = null;
                    if (element.getElementsByTagName("channelId").getLength() > 0) {
                        channelId = element.getElementsByTagName("channelId").item(0).getTextContent();
                    }

                    // Yell Channel ID is optional
                    String yellChannelId = null;
                    if (element.getElementsByTagName("yellChannelId").getLength() > 0) {
                        yellChannelId = element.getElementsByTagName("yellChannelId").item(0).getTextContent();
                    }

                    servers.add(new ServerConfig(name, host, port, channelId, yellChannelId));
                }
            }

        } catch (Exception e) {
            System.err.println("Failed to load server configurations: " + e.getMessage());
            e.printStackTrace();
        }

        return servers;
    }

    /**
     * Get server configuration by name
     *
     * @param name The server name
     * @return ServerConfig or null if not found
     */
    public static ServerConfig getServerByName(String name) {
        List<ServerConfig> servers = loadServerConfigs();
        for (ServerConfig server : servers) {
            if (server.getName().equalsIgnoreCase(name)) {
                return server;
            }
        }
        return null;
    }

    /**
     * Get server configuration by channel ID
     *
     * @param channelId The Discord channel ID
     * @return ServerConfig or null if not found
     */
    public static ServerConfig getServerByChannelId(String channelId) {
        List<ServerConfig> servers = loadServerConfigs();
        for (ServerConfig server : servers) {
            if (channelId.equals(server.getChannelId())) {
                return server;
            }
        }
        return null;
    }

    /**
     * Get server configuration by yell channel ID
     *
     * @param yellChannelId The Discord yell channel ID
     * @return ServerConfig or null if not found
     */
    public static ServerConfig getServerByYellChannelId(String yellChannelId) {
        List<ServerConfig> servers = loadServerConfigs();
        for (ServerConfig server : servers) {
            if (yellChannelId.equals(server.getYellChannelId())) {
                return server;
            }
        }
        return null;
    }
}
