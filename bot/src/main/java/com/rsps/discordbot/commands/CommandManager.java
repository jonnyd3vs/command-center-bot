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
        registerCommand(new RefreshCommandsCommand(this));

        // New commands
        registerCommand(new UnmuteCommand(botConfig));
        registerCommand(new UnbanCommand(botConfig));
        registerCommand(new BanCommand(botConfig));
        registerCommand(new SetPCommand(botConfig));
        registerCommand(new AddPCommand(botConfig));
        registerCommand(new RemovePCommand(botConfig));
        registerCommand(new UpdateCommand(botConfig));
        registerCommand(new DisableUpdateCommand(botConfig));
        registerCommand(new ReleaseCommand(botConfig));
        registerCommand(new ForceVbossCommand(botConfig));
        registerCommand(new LoginAiCommand(botConfig));
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

        // Defer reply immediately to prevent timeout (must respond within 3 seconds)
        // Use ephemeral mode if command requires it (security-sensitive commands)
        event.deferReply(command.isEphemeral()).queue();

        // Check permissions
        if (!hasPermission(event.getMember(), command.getRequiredPermission())) {
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Permission Denied")
                    .setDescription("You don't have permission to execute this command.")
                    .addField("Required Permission", command.getRequiredPermission().toString(), false)
                    .setColor(Color.RED);

            event.getHook().sendMessageEmbeds(embed.build()).queue();
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
     * Implements hierarchical permissions: Owner > Manager > Admin > Moderator
     *
     * @param member The member to check
     * @param requiredLevel The required permission level
     * @return true if the member has permission
     */
    private boolean hasPermission(Member member, Command.PermissionLevel requiredLevel) {
        if (member == null) {
            return false;
        }

        // Check based on permission level (hierarchical)
        switch (requiredLevel) {
            case EVERYONE:
                return true;

            case MODERATOR:
                // Moderator commands work for: Moderator, Admin, Manager, Owner
                return hasModeratorRole(member) || hasAdminRole(member)
                    || hasManagerRole(member) || hasOwnerRole(member) || member.isOwner();

            case ADMIN:
                // Admin commands work for: Admin, Manager, Owner
                return hasAdminRole(member) || hasManagerRole(member)
                    || hasOwnerRole(member) || member.isOwner();

            case MANAGER:
                // Manager commands work for: Manager, Owner
                return hasManagerRole(member) || hasOwnerRole(member) || member.isOwner();

            case OWNER:
                // Owner commands work for: Owner, Developer
                return hasOwnerRole(member) || hasDeveloperRole(member) || member.isOwner();

            case DEVELOPER:
                // Developer commands work for: Developer only
                return hasDeveloperRole(member) || member.isOwner();

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

    /**
     * Check if member has manager role
     *
     * @param member The member to check
     * @return true if member has manager role
     */
    private boolean hasManagerRole(Member member) {
        String managerRoleId = botConfig.getManagerRoleId();
        if (managerRoleId == null || managerRoleId.trim().isEmpty()) {
            return false;
        }

        for (Role role : member.getRoles()) {
            if (role.getId().equals(managerRoleId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if member has owner role
     *
     * @param member The member to check
     * @return true if member has owner role
     */
    private boolean hasOwnerRole(Member member) {
        String ownerRoleId = botConfig.getOwnerRoleId();
        if (ownerRoleId == null || ownerRoleId.trim().isEmpty()) {
            return false;
        }

        for (Role role : member.getRoles()) {
            if (role.getId().equals(ownerRoleId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if member has developer role
     *
     * @param member The member to check
     * @return true if member has developer role
     */
    private boolean hasDeveloperRole(Member member) {
        String developerRoleId = botConfig.getDeveloperRoleId();
        if (developerRoleId == null || developerRoleId.trim().isEmpty()) {
            return false;
        }

        for (Role role : member.getRoles()) {
            if (role.getId().equals(developerRoleId)) {
                return true;
            }
        }

        return false;
    }
}
