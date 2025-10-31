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
 * Command to kick a player offline
 */
public class KickCommand implements Command {

    private final BotConfig botConfig;

    public KickCommand(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("kick", "Kick a player offline")
                .addOption(OptionType.STRING, "username", "The username of the player to kick", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Defer reply to prevent timeout
        event.deferReply().queue();

        // Get channel ID to determine which server
        String channelId = event.getChannel().getId();
        ServerConfig serverConfig = ChannelMapper.getServerForChannel(channelId);

        if (serverConfig == null) {
            event.getHook().sendMessageEmbeds(createErrorEmbed(
                "This command can only be used in server-specific channels (Fantasy, Vale, or Azerite)."
            )).queue();
            return;
        }

        String username = event.getOption("username").getAsString();

        // Create client and execute command
        GameServerClient client = new GameServerClient(serverConfig.getUrl(), botConfig.getApiKey());

        try {
            client.kickPlayer(username);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Player Kicked Successfully")
                    .setColor(Color.RED)
                    .addField("Server", serverConfig.getName(), true)
                    .addField("Player", username, true)
                    .setDescription("The player has been kicked offline.")
                    .setFooter("Executed by " + event.getUser().getName());

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to kick player: " + e.getMessage())).queue();
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
