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

/**
 * Command to mute a player
 */
public class MuteCommand implements Command {

    private final BotConfig botConfig;

    public MuteCommand(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("mute", "Mute a player in the game")
                .addOption(OptionType.STRING, "username", "The username of the player to mute", true)
                .addOption(OptionType.INTEGER, "duration", "Duration in minutes (default: 60)", false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Get channel ID to determine which server
        // (Reply is already deferred by CommandManager)
        String channelId = event.getChannel().getId();
        ServerConfig serverConfig = ChannelMapper.getServerForChannel(channelId);

        if (serverConfig == null) {
            event.getHook().sendMessageEmbeds(createErrorEmbed(
                "This command can only be used in server-specific channels (Fantasy, Vale, or Azerite)."
            )).queue();
            return;
        }

        String username = event.getOption("username").getAsString();
        Integer duration = event.getOption("duration") != null ? event.getOption("duration").getAsInt() : 60; // Default 60 minutes

        // Create client and execute command
        GameServerClient client = new GameServerClient(serverConfig.getUrl(), serverConfig.getApiKey() != null ? serverConfig.getApiKey() : botConfig.getApiKey());

        try {
            client.mutePlayer(username, duration);

            // Calculate duration display
            String durationDisplay;
            if (duration >= 60) {
                int hours = duration / 60;
                int remainingMinutes = duration % 60;
                if (remainingMinutes > 0) {
                    durationDisplay = hours + " hour(s) and " + remainingMinutes + " minute(s)";
                } else {
                    durationDisplay = hours + " hour(s)";
                }
            } else {
                durationDisplay = duration + " minute(s)";
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Player Muted Successfully")
                    .setColor(Color.ORANGE)
                    .addField("Server", serverConfig.getName(), true)
                    .addField("Player", username, true)
                    .addField("Duration", durationDisplay, true)
                    .setFooter("Executed by " + event.getUser().getName());

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to mute player: " + e.getMessage())).queue();
        } finally {
            client.close();
        }
    }

    @Override
    public PermissionLevel getRequiredPermission() {
        return PermissionLevel.MODERATOR;
    }

    private net.dv8tion.jda.api.entities.MessageEmbed createErrorEmbed(String message) {
        return new EmbedBuilder()
                .setTitle("Error")
                .setDescription(message)
                .setColor(Color.RED)
                .build();
    }
}
