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

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("✅ Commands Refreshed Successfully")
                    .setColor(Color.GREEN)
                    .setDescription("All Discord slash commands have been refreshed!")
                    .addField("Total Commands", String.valueOf(commandDataList.size()), true)
                    .addField("Guild", event.getGuild().getName(), true)
                    .addField("Status", "Commands should now be up-to-date in Discord", false)
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
}
