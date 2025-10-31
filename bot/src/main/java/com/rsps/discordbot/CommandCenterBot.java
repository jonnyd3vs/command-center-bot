package com.rsps.discordbot;

import com.rsps.discordbot.commands.Command;
import com.rsps.discordbot.commands.CommandManager;
import com.rsps.discordbot.config.BotConfig;
import com.rsps.discordbot.config.ServerConfig;
import com.rsps.discordbot.yell.YellServer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.util.ArrayList;
import java.util.List;

/**
 * Main class for the RSPS Command Center Discord Bot
 */
public class CommandCenterBot {

    private static JDA jda;
    private static BotConfig botConfig;
    private static CommandManager commandManager;
    private static YellServer yellServer;

    public static void main(String[] args) {
        System.out.println("Starting RSPS Command Center Bot...");

        try {
            // Load configuration
            botConfig = new BotConfig();
            System.out.println("Configuration loaded successfully");

            // Load server configurations
            List<ServerConfig> servers = ServerConfig.loadServerConfigs();
            System.out.println("Loaded " + servers.size() + " server configuration(s)");

            // Build JDA instance first (needed for YellServer)
            jda = JDABuilder.createDefault(botConfig.getBotToken())
                    .setStatus(OnlineStatus.ONLINE)
                    .setActivity(Activity.watching("RSPS Servers"))
                    .enableIntents(
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .build();

            // Wait for JDA to be ready
            jda.awaitReady();
            System.out.println("Bot is online!");

            // Start yell server
            yellServer = new YellServer(botConfig, servers, jda, botConfig.getYellServerPort());
            yellServer.start();

            // Create command manager (needs YellServer's message queue)
            commandManager = new CommandManager(botConfig, yellServer.getMessageQueue());
            System.out.println("Command manager initialized");

            // Add command manager as event listener
            jda.addEventListener(commandManager);

            // Register slash commands (this will also update the command list channel)
            registerSlashCommands();

            System.out.println("RSPS Command Center Bot is ready!");

        } catch (Exception e) {
            System.err.println("Failed to start bot: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down bot...");
            if (yellServer != null) {
                yellServer.stop();
            }
            if (jda != null) {
                jda.shutdown();
            }
        }));
    }

    /**
     * Register all slash commands with Discord
     */
    private static void registerSlashCommands() {
        System.out.println("Clearing old commands and registering new slash commands...");

        List<net.dv8tion.jda.api.interactions.commands.build.CommandData> commandDataList = new ArrayList<>();

        for (Command command : commandManager.getCommands().values()) {
            commandDataList.add(command.getCommandData());
        }

        String guildId = "1433696315602243748";

        try {
            // Clear all global commands first (synchronous)
            System.out.println("Clearing global commands...");
            jda.updateCommands().complete();
            System.out.println("Global commands cleared");

            // Clear and register guild commands (synchronous)
            System.out.println("Registering " + commandDataList.size() + " guild commands...");
            jda.getGuildById(guildId).updateCommands().addCommands(commandDataList).complete();
            System.out.println("Successfully registered " + commandDataList.size() + " commands to guild " + guildId);

            // Update command list channel after commands are registered
            updateCommandListChannel();

        } catch (Exception e) {
            System.err.println("Failed to register commands: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update the command list channel with current commands
     */
    private static void updateCommandListChannel() {
        // Run in separate thread to avoid blocking
        new Thread(() -> {
            try {
                // Small delay to ensure Discord has processed the command registration
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String commandListChannelId = "1433770020537896960";
            net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel = jda.getTextChannelById(commandListChannelId);

            if (channel == null) {
                System.err.println("Command list channel not found: " + commandListChannelId);
                return;
            }

            System.out.println("Updating command list channel...");
            System.out.println("Total commands to display: " + commandManager.getCommands().size());

            // Delete all messages in the channel
            try {
                channel.getIterableHistory().complete().forEach(message -> {
                    try {
                        message.delete().complete();
                    } catch (Exception e) {
                        System.err.println("Failed to delete message: " + e.getMessage());
                    }
                });
                System.out.println("Cleared old messages from command list channel");
            } catch (Exception e) {
                System.err.println("Failed to clear messages: " + e.getMessage());
            }

            // Build command list embed
            net.dv8tion.jda.api.EmbedBuilder embed = new net.dv8tion.jda.api.EmbedBuilder()
                    .setTitle("🤖 Command Center - Available Commands")
                    .setDescription("All bot commands for managing RSPS servers")
                    .setColor(java.awt.Color.BLUE)
                    .setTimestamp(java.time.Instant.now());

            // Sort commands alphabetically
            java.util.List<Command> sortedCommands = new java.util.ArrayList<>(commandManager.getCommands().values());
            sortedCommands.sort((c1, c2) -> c1.getCommandData().getName().compareTo(c2.getCommandData().getName()));

            // Add each command as a field
            int commandCount = 0;
            for (Command command : sortedCommands) {
                net.dv8tion.jda.api.interactions.commands.build.CommandData cmdData = command.getCommandData();

                // Cast to SlashCommandData to access description and options
                if (cmdData instanceof net.dv8tion.jda.api.interactions.commands.build.SlashCommandData) {
                    net.dv8tion.jda.api.interactions.commands.build.SlashCommandData slashCmd =
                        (net.dv8tion.jda.api.interactions.commands.build.SlashCommandData) cmdData;

                    StringBuilder description = new StringBuilder(slashCmd.getDescription());

                    // Add options/parameters to description
                    if (!slashCmd.getOptions().isEmpty()) {
                        description.append("\n**Parameters:**");
                        for (net.dv8tion.jda.api.interactions.commands.build.OptionData option : slashCmd.getOptions()) {
                            description.append("\n• `").append(option.getName()).append("`: ").append(option.getDescription());
                            if (option.isRequired()) {
                                description.append(" *(required)*");
                            }
                        }
                    }

                    // Add permission level
                    description.append("\n**Permission:** ").append(command.getRequiredPermission().toString());

                    embed.addField("/" + slashCmd.getName(), description.toString(), false);
                    commandCount++;
                    System.out.println("Added command to list: /" + slashCmd.getName());
                }
            }

            embed.setFooter("Bot restarted • " + commandCount + " commands available • " +
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // Send the embed
            try {
                channel.sendMessageEmbeds(embed.build()).complete();
                System.out.println("Command list updated successfully with " + commandCount + " commands");
            } catch (Exception e) {
                System.err.println("Failed to send command list: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Get the JDA instance
     *
     * @return JDA instance
     */
    public static JDA getJda() {
        return jda;
    }

    /**
     * Get the bot configuration
     *
     * @return BotConfig instance
     */
    public static BotConfig getBotConfig() {
        return botConfig;
    }
}
