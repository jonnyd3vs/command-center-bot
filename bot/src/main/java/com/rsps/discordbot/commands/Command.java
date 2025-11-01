package com.rsps.discordbot.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * Base interface for all Discord slash commands
 */
public interface Command {

    /**
     * Get the command data for registration with Discord
     *
     * @return CommandData for this command
     */
    CommandData getCommandData();

    /**
     * Execute the command
     *
     * @param event The slash command interaction event
     */
    void execute(SlashCommandInteractionEvent event);

    /**
     * Get the minimum permission level required to execute this command
     *
     * @return Permission level (EVERYONE, MODERATOR, ADMIN, MANAGER, OWNER)
     */
    PermissionLevel getRequiredPermission();

    /**
     * Whether this command's replies should be ephemeral (only visible to the user who ran it)
     * Override this method to return true for security-sensitive commands
     *
     * @return true if replies should be ephemeral, false otherwise (default)
     */
    default boolean isEphemeral() {
        return false;
    }

    /**
     * Permission levels for commands (in hierarchical order)
     */
    enum PermissionLevel {
        EVERYONE,
        MODERATOR,
        ADMIN,
        MANAGER,
        OWNER,
        DEVELOPER
    }
}
