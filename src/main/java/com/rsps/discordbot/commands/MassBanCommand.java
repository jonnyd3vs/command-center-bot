package com.rsps.discordbot.commands;

import com.rsps.discordbot.client.GameServerClient;
import com.rsps.discordbot.config.BotConfig;
import com.rsps.discordbot.config.ServerConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.awt.Color;

/**
 * Command to ban multiple players
 */
public class MassBanCommand implements Command {

    private final BotConfig botConfig;

    public MassBanCommand(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("massban", "Ban a player and their associated accounts")
                .addOption(OptionType.STRING, "server", "The server name (e.g., VoidX, Kingdom)", true)
                .addOption(OptionType.STRING, "player", "The player name", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Defer reply to prevent timeout
        event.deferReply().queue();

        String serverName = event.getOption("server").getAsString();
        String playerName = event.getOption("player").getAsString();

        // Get server config
        ServerConfig serverConfig = ServerConfig.getServerByName(serverName);
        if (serverConfig == null) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Server not found: " + serverName)).queue();
            return;
        }

        // Create client and execute command
        GameServerClient client = new GameServerClient(serverConfig.getUrl(), botConfig.getApiKey());

        try {
            client.massBan(playerName);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Mass Ban Executed")
                    .setColor(Color.ORANGE)
                    .addField("Server", serverConfig.getName(), true)
                    .addField("Player", playerName, true)
                    .setDescription("Player and associated accounts have been banned")
                    .setFooter("Executed by " + event.getUser().getName());

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to execute mass ban: " + e.getMessage())).queue();
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
