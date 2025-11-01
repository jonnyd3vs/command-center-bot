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
import java.util.List;
import java.util.Map;

/**
 * Command to find items by name
 */
public class FindItemCommand implements Command {

    private final BotConfig botConfig;

    public FindItemCommand(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("finditem", "Search for items by name")
                .addOption(OptionType.STRING, "name", "The item name to search for", true);
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

        String itemName = event.getOption("name").getAsString();

        // Create client and execute command
        GameServerClient client = new GameServerClient(serverConfig.getUrl(), serverConfig.getApiKey() != null ? serverConfig.getApiKey() : botConfig.getApiKey());

        try {
            Map<String, Object> response = client.findItem(itemName);

            // Extract items list from response
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

            if (items == null || items.isEmpty()) {
                event.getHook().sendMessageEmbeds(createErrorEmbed("No items found matching: " + itemName)).queue();
                return;
            }

            // Build the embed with items
            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Item Search Results")
                    .setColor(Color.BLUE)
                    .setDescription("Found " + items.size() + " item(s) matching: **" + itemName + "**")
                    .setFooter("Server: " + serverConfig.getName() + " | Requested by " + event.getUser().getName());

            // Add items to embed (Discord has field limits, so we'll format as description)
            StringBuilder itemList = new StringBuilder();
            int count = 0;
            for (Map<String, Object> item : items) {
                if (count >= 50) break; // Limit to 50 items

                Object idObj = item.get("id");
                String itemNameStr = (String) item.get("name");

                // Convert id to int if it's a double
                int itemId;
                if (idObj instanceof Double) {
                    itemId = ((Double) idObj).intValue();
                } else if (idObj instanceof Integer) {
                    itemId = (Integer) idObj;
                } else {
                    itemId = Integer.parseInt(idObj.toString());
                }

                itemList.append("**").append(itemNameStr).append("** - ID: `").append(itemId).append("`\n");
                count++;
            }

            embed.addField("Items", itemList.toString(), false);

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to search items: " + e.getMessage())).queue();
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
