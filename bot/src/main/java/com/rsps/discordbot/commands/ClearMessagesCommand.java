package com.rsps.discordbot.commands;

import com.rsps.discordbot.yell.YellMessageQueue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.awt.Color;

/**
 * Command to clear all pending yell messages from the queue
 */
public class ClearMessagesCommand implements Command {

    private final YellMessageQueue yellMessageQueue;

    public ClearMessagesCommand(YellMessageQueue yellMessageQueue) {
        this.yellMessageQueue = yellMessageQueue;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("clear-messages", "Clear all pending yell messages from the queue");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Defer reply to prevent timeout
        event.deferReply().queue();

        try {
            int clearedCount = yellMessageQueue.clearMessages();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Messages Cleared")
                    .setColor(Color.ORANGE)
                    .setDescription("Cleared " + clearedCount + " pending yell message(s) from the queue.")
                    .setFooter("Executed by " + event.getUser().getName());

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to clear messages: " + e.getMessage())).queue();
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
