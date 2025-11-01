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
import java.util.Map;

/**
 * Command to give items to all online players
 */
public class GiveAllCommand implements Command {

    private final BotConfig botConfig;

    public GiveAllCommand(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("giveall", "Give an item to all online players")
                .addOption(OptionType.INTEGER, "itemid", "The item ID number (e.g., 995 for coins)", true)
                .addOption(OptionType.INTEGER, "amount", "The quantity of items to give (must be positive)", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Get channel ID to determine which server
        // (Reply is already deferred by CommandManager)
        String channelId = event.getChannel().getId();
        ServerConfig serverConfig = ChannelMapper.getServerForChannel(channelId);

        if (serverConfig == null) {
            event.getHook().sendMessageEmbeds(createErrorEmbed(
                "This command can only be used in server-specific channels (Fantasy, VoidX, or Kingdom)."
            )).queue();
            return;
        }

        int itemId = event.getOption("itemid").getAsInt();
        int amount = event.getOption("amount").getAsInt();

        // Validate input
        if (amount <= 0) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Amount must be greater than 0")).queue();
            return;
        }

        // Create client and execute command
        GameServerClient client = new GameServerClient(serverConfig.getUrl(), serverConfig.getApiKey() != null ? serverConfig.getApiKey() : botConfig.getApiKey());

        try {
            Map<String, Object> response = client.giveAll(itemId, amount);

            // Extract response data
            String itemName = response.getOrDefault("itemName", "Unknown Item").toString();
            int playersGiven = response.containsKey("playersGiven")
                ? ((Number) response.get("playersGiven")).intValue()
                : 0;

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Items Given to All Players")
                    .setColor(Color.GREEN)
                    .addField("Server", serverConfig.getName(), true)
                    .addField("Item", itemName, true)
                    .addField("Item ID", String.valueOf(itemId), true)
                    .addField("Amount per Player", String.valueOf(amount), true)
                    .addField("Players Received", String.valueOf(playersGiven), true)
                    .setFooter("Executed by " + event.getUser().getName());

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to give items: " + e.getMessage())).queue();
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
