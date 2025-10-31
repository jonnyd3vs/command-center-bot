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

            // Update command list channel
            updateCommandListChannel();

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

        // Register commands to specific guild for instant updates
        String guildId = "1433696315602243748";
        jda.getGuildById(guildId).updateCommands().addCommands(commandDataList).queue(
                success -> System.out.println("Successfully registered " + commandDataList.size() + " commands to guild " + guildId),
                error -> System.err.println("Failed to register commands: " + error.getMessage())
        );
    }

    /**
     * Update the command list channel with current commands
     */
    private static void updateCommandListChannel() {
        String commandListChannelId = "1433770020537896960";
        net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel = jda.getTextChannelById(commandListChannelId);

        if (channel == null) {
            System.err.println("Command list channel not found: " + commandListChannelId);
            return;
        }

        System.out.println("Updating command list channel...");

        // Delete all messages in the channel
        channel.getIterableHistory().queue(messages -> {
            if (!messages.isEmpty()) {
                channel.purgeMessages(messages);
                System.out.println("Cleared " + messages.size() + " messages from command list channel");
            }

            // Build command list embed
            net.dv8tion.jda.api.EmbedBuilder embed = new net.dv8tion.jda.api.EmbedBuilder()
                    .setTitle("Available Commands")
                    .setDescription("List of all available bot commands")
                    .setColor(java.awt.Color.BLUE)
                    .setTimestamp(java.time.Instant.now());

            // Add each command as a field
            for (Command command : commandManager.getCommands().values()) {
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

                    embed.addField("/" + slashCmd.getName(), description.toString(), false);
                }
            }

            embed.setFooter("Bot restarted at " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // Send the embed
            channel.sendMessageEmbeds(embed.build()).queue(
                    success -> System.out.println("Command list updated successfully"),
                    error -> System.err.println("Failed to update command list: " + error.getMessage())
            );
        });
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
