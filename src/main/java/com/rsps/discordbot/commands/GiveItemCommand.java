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
 * Command to give items to players
 */
public class GiveItemCommand implements Command {

    private final BotConfig botConfig;

    public GiveItemCommand(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("giveitem", "Give an item to a player")
                .addOption(OptionType.STRING, "server", "The server name (e.g., VoidX, Kingdom)", true)
                .addOption(OptionType.STRING, "player", "The player name", true)
                .addOption(OptionType.INTEGER, "itemid", "The item ID", true)
                .addOption(OptionType.INTEGER, "amount", "The amount", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Defer reply to prevent timeout
        event.deferReply().queue();

        String serverName = event.getOption("server").getAsString();
        String playerName = event.getOption("player").getAsString();
        int itemId = event.getOption("itemid").getAsInt();
        int amount = event.getOption("amount").getAsInt();

        // Validate input
        if (amount <= 0) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Amount must be greater than 0")).queue();
            return;
        }

        // Get server config
        ServerConfig serverConfig = ServerConfig.getServerByName(serverName);
        if (serverConfig == null) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Server not found: " + serverName)).queue();
            return;
        }

        // Create client and execute command
        GameServerClient client = new GameServerClient(serverConfig.getUrl(), botConfig.getApiKey());

        try {
            client.giveItem(playerName, itemId, amount);

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Item Given Successfully")
                    .setColor(Color.GREEN)
                    .addField("Server", serverConfig.getName(), true)
                    .addField("Player", playerName, true)
                    .addField("Item ID", String.valueOf(itemId), true)
                    .addField("Amount", String.valueOf(amount), true)
                    .setFooter("Executed by " + event.getUser().getName());

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to give item: " + e.getMessage())).queue();
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
