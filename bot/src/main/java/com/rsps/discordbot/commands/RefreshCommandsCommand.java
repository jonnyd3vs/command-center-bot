package com.rsps.discordbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.awt.Color;

/**
 * Command to manually refresh Discord slash commands
 * This will re-register all commands to fix duplicates
 */
public class RefreshCommandsCommand implements Command {

    @Override
    public CommandData getCommandData() {
        return Commands.slash("refresh-commands", "Manually refresh all Discord slash commands");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        try {
            // Get current guild commands from Discord
            int commandCount = event.getGuild().retrieveCommands().complete().size();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("🔄 Refreshing Commands...")
                    .setColor(Color.ORANGE)
                    .setDescription("Please restart the bot to fully refresh commands.\n\n" +
                            "Current commands: " + commandCount + "\n\n" +
                            "**To fix duplicates:**\n" +
                            "1. Restart the bot\n" +
                            "2. Old commands will be cleared\n" +
                            "3. Fresh commands will be registered")
                    .setFooter("Executed by " + event.getUser().getName());

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to check commands: " + e.getMessage())).queue();
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
