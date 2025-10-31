package com.rsps.discordbot;

import com.rsps.discordbot.commands.Command;
import com.rsps.discordbot.commands.CommandManager;
import com.rsps.discordbot.config.BotConfig;
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

    public static void main(String[] args) {
        System.out.println("Starting RSPS Command Center Bot...");

        try {
            // Load configuration
            botConfig = new BotConfig();
            System.out.println("Configuration loaded successfully");

            // Create command manager
            commandManager = new CommandManager(botConfig);
            System.out.println("Command manager initialized");

            // Build JDA instance
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
                    .addEventListeners(commandManager)
                    .build();

            // Wait for JDA to be ready
            jda.awaitReady();
            System.out.println("Bot is online!");

            // Register slash commands
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
            if (jda != null) {
                jda.shutdown();
            }
        }));
    }

    /**
     * Register all slash commands with Discord
     */
    private static void registerSlashCommands() {
        System.out.println("Registering slash commands...");

        List<net.dv8tion.jda.api.interactions.commands.build.CommandData> commandDataList = new ArrayList<>();

        for (Command command : commandManager.getCommands().values()) {
            commandDataList.add(command.getCommandData());
        }

        // Register commands globally (takes up to 1 hour to update)
        // For immediate updates during development, use guild.updateCommands() instead
        jda.updateCommands().addCommands(commandDataList).queue(
                success -> System.out.println("Successfully registered " + commandDataList.size() + " commands"),
                error -> System.err.println("Failed to register commands: " + error.getMessage())
        );
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
