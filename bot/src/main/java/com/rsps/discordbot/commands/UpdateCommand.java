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
 * Command to initiate a server update/restart
 * Restricted to owner-level access
 */
public class UpdateCommand implements Command {

    private final BotConfig botConfig;

    public UpdateCommand(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("update", "Initiate a server update/restart")
                .addOption(OptionType.INTEGER, "time", "Number of seconds until server restarts", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String channelId = event.getChannel().getId();
        ServerConfig serverConfig = ChannelMapper.getServerForChannel(channelId);

        if (serverConfig == null) {
            event.getHook().sendMessageEmbeds(createErrorEmbed(
                "This command can only be used in server-specific channels (Fantasy, Vale, or Azerite)."
            )).queue();
            return;
        }

        int timeInSeconds = event.getOption("time").getAsInt();

        GameServerClient client = new GameServerClient(serverConfig.getUrl(), serverConfig.getApiKey() != null ? serverConfig.getApiKey() : botConfig.getApiKey());

        try {
            client.update(timeInSeconds);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Server Update Initiated")
                    .setColor(Color.ORANGE)
                    .addField("Server", serverConfig.getName(), true)
                    .addField("Time Until Restart", timeInSeconds + " seconds", true)
                    .setDescription("The server will save and restart in " + timeInSeconds + " seconds.")
                    .setFooter("Executed by " + event.getUser().getName());

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to initiate update: " + e.getMessage())).queue();
        } finally {
            client.close();
        }
    }

    @Override
    public PermissionLevel getRequiredPermission() {
        return PermissionLevel.OWNER;
    }

    private net.dv8tion.jda.api.entities.MessageEmbed createErrorEmbed(String message) {
        return new EmbedBuilder()
                .setTitle("Error")
                .setDescription(message)
                .setColor(Color.RED)
                .build();
    }
}
