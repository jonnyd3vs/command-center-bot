package com.rsps.discordbot.commands;

import com.rsps.discordbot.client.GameServerClient;
import com.rsps.discordbot.config.BotConfig;
import com.rsps.discordbot.config.ChannelMapper;
import com.rsps.discordbot.config.ServerConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.awt.Color;

/**
 * Command to force spawn a variable boss
 * Restricted to moderator+ access
 */
public class ForceVbossCommand implements Command {

    private final BotConfig botConfig;

    public ForceVbossCommand(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("force-vboss", "Force spawn a variable boss");
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

        GameServerClient client = new GameServerClient(serverConfig.getUrl(), serverConfig.getApiKey() != null ? serverConfig.getApiKey() : botConfig.getApiKey());

        try {
            client.forceVboss();

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Variable Boss Spawned")
                    .setColor(Color.CYAN)
                    .addField("Server", serverConfig.getName(), true)
                    .setDescription("A variable boss has been force spawned!")
                    .setFooter("Executed by " + event.getUser().getName());

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to force spawn variable boss: " + e.getMessage())).queue();
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
