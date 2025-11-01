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
 * Command to ban a player
 */
public class BanCommand implements Command {

    private final BotConfig botConfig;

    public BanCommand(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("ban", "Ban a player from the game")
                .addOption(OptionType.STRING, "username", "The username of the player to ban", true)
                .addOption(OptionType.INTEGER, "duration", "Duration in days (default: 7)", false);
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
        Integer duration = event.getOption("duration") != null ? event.getOption("duration").getAsInt() : 7; // Default 7 days

        // Create client and execute command
        GameServerClient client = new GameServerClient(serverConfig.getUrl(), botConfig.getApiKey());

        try {
            client.banPlayer(username, duration);

            // Calculate duration display
            String durationDisplay;
            if (duration == 1) {
                durationDisplay = "1 day";
            } else if (duration >= 365) {
                int years = duration / 365;
                if (years == 1) {
                    durationDisplay = "1 year";
                } else {
                    durationDisplay = years + " years";
                }
            } else {
                durationDisplay = duration + " days";
            }

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Player Banned Successfully")
                    .setColor(Color.RED)
                    .addField("Server", serverConfig.getName(), true)
                    .addField("Player", username, true)
                    .addField("Duration", durationDisplay, true)
                    .setFooter("Executed by " + event.getUser().getName());

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to ban player: " + e.getMessage())).queue();
        } finally {
            client.close();
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
