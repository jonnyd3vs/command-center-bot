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
                .addOption(OptionType.STRING, "player", "The username of the player who will receive the offers", true)
                .addOption(OptionType.STRING, "offertype", "The type of special offer to grant (e.g., starter pack, bonus rewards)", false)
                .addOption(OptionType.STRING, "offerdata", "Additional data or parameters for the offer (JSON format if applicable)", false);
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

        String playerName = event.getOption("player").getAsString();
        String offerType = event.getOption("offertype") != null ? event.getOption("offertype").getAsString() : "";
        String offerData = event.getOption("offerdata") != null ? event.getOption("offerdata").getAsString() : "";

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
