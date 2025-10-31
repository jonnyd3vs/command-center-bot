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
 * Command to give special offers to players
 */
public class GiveOffersCommand implements Command {

    private final BotConfig botConfig;

    public GiveOffersCommand(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("giveoffers", "Give special offers to a player")
                .addOption(OptionType.STRING, "server", "The server name (e.g., VoidX, Kingdom)", true)
                .addOption(OptionType.STRING, "player", "The player name", true)
                .addOption(OptionType.STRING, "offertype", "The type of offer", false)
                .addOption(OptionType.STRING, "offerdata", "Additional offer data", false);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Defer reply to prevent timeout
        event.deferReply().queue();

        String serverName = event.getOption("server").getAsString();
        String playerName = event.getOption("player").getAsString();
        String offerType = event.getOption("offertype") != null ? event.getOption("offertype").getAsString() : "";
        String offerData = event.getOption("offerdata") != null ? event.getOption("offerdata").getAsString() : "";

        // Get server config
        ServerConfig serverConfig = ServerConfig.getServerByName(serverName);
        if (serverConfig == null) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Server not found: " + serverName)).queue();
            return;
        }

        // Create client and execute command
        GameServerClient client = new GameServerClient(serverConfig.getUrl(), botConfig.getApiKey());

        try {
            client.giveOffers(playerName, offerType, offerData);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Offers Given Successfully")
                    .setColor(Color.CYAN)
                    .addField("Server", serverConfig.getName(), true)
                    .addField("Player", playerName, true)
                    .setFooter("Executed by " + event.getUser().getName());

            if (!offerType.isEmpty()) {
                embed.addField("Offer Type", offerType, true);
            }

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to give offers: " + e.getMessage())).queue();
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
