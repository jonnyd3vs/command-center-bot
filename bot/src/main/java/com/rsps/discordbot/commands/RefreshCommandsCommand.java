package com.rsps.discordbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Command to manually refresh Discord slash commands
 * This will clear and re-register all commands without restarting the bot
 */
public class RefreshCommandsCommand implements Command {

    private final CommandManager commandManager;

    public RefreshCommandsCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("refresh-commands", "Manually refresh all Discord slash commands");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        try {
            System.out.println("\n=== Manual Command Refresh Triggered by " + event.getUser().getName() + " ===");

            // Get all command data
            List<net.dv8tion.jda.api.interactions.commands.build.CommandData> commandDataList = new ArrayList<>();
            for (Command command : commandManager.getCommands().values()) {
                commandDataList.add(command.getCommandData());
            }

            // Clear global commands
            System.out.println("Clearing global commands...");
            event.getJDA().updateCommands().complete();
            System.out.println("  ✓ Global commands cleared");

            // Update guild commands
            System.out.println("Updating " + commandDataList.size() + " guild commands...");
            event.getGuild().updateCommands().addCommands(commandDataList).complete();
            System.out.println("  ✓ Guild commands updated");

            // Also update the command list channel
            System.out.println("Updating command list channel...");
            updateCommandListChannel(event.getJDA());

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("✅ Commands Refreshed Successfully")
                    .setColor(Color.GREEN)
                    .setDescription("All Discord slash commands have been refreshed!")
                    .addField("Total Commands", String.valueOf(commandDataList.size()), true)
                    .addField("Guild", event.getGuild().getName(), true)
                    .addField("Status", "✓ Commands updated in Discord\n✓ Command list channel updated", false)
                    .setFooter("Executed by " + event.getUser().getName());

            event.getHook().sendMessageEmbeds(embed.build()).queue();

            System.out.println("=== Command Refresh Complete ===\n");

        } catch (Exception e) {
            System.err.println("✗ Failed to refresh commands: " + e.getMessage());
            e.printStackTrace();
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to refresh commands: " + e.getMessage())).queue();
        }
    }

    @Override
    public PermissionLevel getRequiredPermission() {
        return PermissionLevel.ADMIN;
    }

    private net.dv8tion.jda.api.entities.MessageEmbed createErrorEmbed(String message) {
        return new EmbedBuilder()
                .setTitle("Error")
                .setDescription(message)
                .setColor(Color.RED)
                .build();
    }

    /**
     * Update the command list channel with current commands
     */
    private void updateCommandListChannel(net.dv8tion.jda.api.JDA jda) {
        String commandListChannelId = "1433770020537896960";

        net.dv8tion.jda.api.entities.channel.concrete.TextChannel channel = jda.getTextChannelById(commandListChannelId);

        if (channel == null) {
            System.err.println("Command list channel not found: " + commandListChannelId);
            return;
        }

        try {
            // Clear old messages
            channel.getIterableHistory().complete().forEach(message -> {
                try {
                    message.delete().complete();
                } catch (Exception e) {
                    // Ignore
                }
            });

            // Build command list embed
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🤖 Command Center - Available Commands")
                    .setDescription("All bot commands for managing RSPS servers")
                    .setColor(Color.BLUE)
                    .setTimestamp(java.time.Instant.now());

            // Sort commands alphabetically
            List<Command> sortedCommands = new ArrayList<>(commandManager.getCommands().values());
            sortedCommands.sort((c1, c2) -> c1.getCommandData().getName().compareTo(c2.getCommandData().getName()));

            // Add each command as a field
            int commandCount = 0;
            for (Command command : sortedCommands) {
                net.dv8tion.jda.api.interactions.commands.build.CommandData cmdData = command.getCommandData();

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
                }
            }

            embed.setFooter("Commands refreshed • " + commandCount + " commands available • " +
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            channel.sendMessageEmbeds(embed.build()).complete();
            System.out.println("  ✓ Command list channel updated with " + commandCount + " commands");

        } catch (Exception e) {
            System.err.println("Failed to update command list channel: " + e.getMessage());
        }
    }
}
