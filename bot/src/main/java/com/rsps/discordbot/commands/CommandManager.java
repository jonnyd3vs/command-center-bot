package com.rsps.discordbot.commands;

import com.rsps.discordbot.config.BotConfig;
import com.rsps.discordbot.yell.YellMessageQueue;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages all bot commands and handles permission checking
 */
public class CommandManager extends ListenerAdapter {

    private final Map<String, Command> commands;
    private final BotConfig botConfig;
    private final YellMessageQueue yellMessageQueue;

    public CommandManager(BotConfig botConfig, YellMessageQueue yellMessageQueue) {
        this.botConfig = botConfig;
        this.yellMessageQueue = yellMessageQueue;
        this.commands = new HashMap<>();
        registerCommands();
    }

    /**
     * Register all available commands
     */
    private void registerCommands() {
        registerCommand(new GiveItemCommand(botConfig));
        registerCommand(new MassBanCommand(botConfig));
        registerCommand(new GiveOffersCommand(botConfig));
        registerCommand(new SetPasswordCommand(botConfig));
        registerCommand(new FindItemCommand(botConfig));
        registerCommand(new MuteCommand(botConfig));
        registerCommand(new CheckPinCommand(botConfig));
        registerCommand(new ClearProgressCommand(botConfig));
        registerCommand(new KickCommand(botConfig));
        registerCommand(new ClearMessagesCommand(yellMessageQueue));
    }

    /**
     * Register a single command
     *
     * @param command The command to register
     */
    private void registerCommand(Command command) {
        String commandName = command.getCommandData().getName();
        commands.put(commandName, command);
    }

    /**
     * Get all registered commands
     *
     * @return Map of command names to Command objects
     */
    public Map<String, Command> getCommands() {
        return commands;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        Command command = commands.get(commandName);

        if (command == null) {
            return;
        }

        // Check permissions
        if (!hasPermission(event.getMember(), command.getRequiredPermission())) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Permission Denied")
                    .setDescription("You don't have permission to execute this command.")
                    .addField("Required Permission", command.getRequiredPermission().toString(), false)
                    .setColor(Color.RED);

            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            return;
        }

        // Execute the command
        try {
            command.execute(event);
        } catch (Exception e) {
            e.printStackTrace();
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Command Error")
                    .setDescription("An error occurred while executing the command: " + e.getMessage())
                    .setColor(Color.RED);

            if (event.isAcknowledged()) {
                event.getHook().sendMessageEmbeds(embed.build()).queue();
            } else {
                event.replyEmbeds(embed.build()).setEphemeral(true).queue();
            }
        }
    }

    /**
     * Check if a member has the required permission level
     *
     * @param member The member to check
     * @param requiredLevel The required permission level
     * @return true if the member has permission
     */
    private boolean hasPermission(Member member, Command.PermissionLevel requiredLevel) {
        if (member == null) {
            return false;
        }

        // Server owner always has permission
        if (member.isOwner()) {
            return true;
        }

        // Check based on permission level
        switch (requiredLevel) {
            case EVERYONE:
                return true;

            case MODERATOR:
                return hasModeratorRole(member) || hasAdminRole(member);

            case ADMIN:
                return hasAdminRole(member);

            default:
                return false;
        }
    }

    /**
     * Check if member has admin role
     *
     * @param member The member to check
     * @return true if member has admin role
     */
    private boolean hasAdminRole(Member member) {
        String adminRoleId = botConfig.getAdminRoleId();
        if (adminRoleId == null || adminRoleId.trim().isEmpty()) {
            // If no admin role configured, check for Administrator permission
            return member.hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR);
        }

        for (Role role : member.getRoles()) {
            if (role.getId().equals(adminRoleId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if member has moderator role
     *
     * @param member The member to check
     * @return true if member has moderator role
     */
    private boolean hasModeratorRole(Member member) {
        String moderatorRoleId = botConfig.getModeratorRoleId();
        if (moderatorRoleId == null || moderatorRoleId.trim().isEmpty()) {
            // If no moderator role configured, check for Manage Server permission
            return member.hasPermission(net.dv8tion.jda.api.Permission.MANAGE_SERVER);
        }

        for (Role role : member.getRoles()) {
            if (role.getId().equals(moderatorRoleId)) {
                return true;
            }
        }

        return false;
    }
}
