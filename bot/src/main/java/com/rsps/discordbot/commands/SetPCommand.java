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
 * Command to set fake player count
 * Restricted to owner-level access
 */
public class SetPCommand implements Command {

    private final BotConfig botConfig;

    public SetPCommand(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("setp", "Set the fake player count")
                .addOption(OptionType.INTEGER, "amount", "The amount to set the fake player count to", true);
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

        int amount = event.getOption("amount").getAsInt();

        GameServerClient client = new GameServerClient(serverConfig.getUrl(), serverConfig.getApiKey() != null ? serverConfig.getApiKey() : botConfig.getApiKey());

        try {
            client.setPlayerCount(amount);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Fake Player Count Set")
                    .setColor(Color.BLUE)
                    .addField("Server", serverConfig.getName(), true)
                    .addField("New Count", String.valueOf(amount), true)
                    .setFooter("Executed by " + event.getUser().getName());

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to set player count: " + e.getMessage())).queue();
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
