package com.rsps.discordbot.commands;

import com.rsps.discordbot.client.GameServerClient;
import com.rsps.discordbot.config.BotConfig;
import com.rsps.discordbot.config.ChannelMapper;
import com.rsps.discordbot.config.ServerConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.awt.Color;
import java.util.Map;

/**
 * Command to reset/set player passwords
 */
public class SetPasswordCommand implements Command {

    private final BotConfig botConfig;

    public SetPasswordCommand(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("setpassword", "Reset a player's password")
                .addOption(OptionType.STRING, "player", "The username of the player whose password will be reset", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Get channel ID to determine which server
        // (Reply is already deferred as ephemeral by CommandManager)
        String channelId = event.getChannel().getId();
        ServerConfig serverConfig = ChannelMapper.getServerForChannel(channelId);

        if (serverConfig == null) {
            event.getHook().sendMessageEmbeds(createErrorEmbed(
                "This command can only be used in server-specific channels (Fantasy, Vale, or Azerite)."
            )).queue();
            return;
        }

        String playerName = event.getOption("player").getAsString();

        // Create client and execute command
        GameServerClient client = new GameServerClient(serverConfig.getUrl(), serverConfig.getApiKey() != null ? serverConfig.getApiKey() : botConfig.getApiKey());

        try {
            Map<String, Object> response = client.setPassword(playerName);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Password Reset Successfully")
                    .setColor(Color.GREEN)
                    .addField("Server", serverConfig.getName(), true)
                    .addField("Player", playerName, true)
                    .setFooter("Executed by " + event.getUser().getName());

            // Add new password if available in response
            if (response.containsKey("newPassword")) {
                embed.addField("New Password", (String) response.get("newPassword"), false);
            }

            embed.setDescription("This message is only visible to you for security reasons.");

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to reset password: " + e.getMessage())).queue();
        } finally {
            client.close();
        }
    }

    @Override
    public PermissionLevel getRequiredPermission() {
        return PermissionLevel.ADMIN;
    }

    @Override
    public boolean isEphemeral() {
        return true;  // Password resets should only be visible to the command user
    }

    private net.dv8tion.jda.api.entities.MessageEmbed createErrorEmbed(String message) {
        return new EmbedBuilder()
                .setTitle("Error")
                .setDescription(message)
                .setColor(Color.RED)
                .build();
    }
}
