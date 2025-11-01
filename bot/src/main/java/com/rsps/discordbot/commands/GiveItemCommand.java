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
                .addOption(OptionType.STRING, "player", "The username of the player who will receive the item", true)
                .addOption(OptionType.INTEGER, "itemid", "The item ID number (e.g., 995 for coins)", true)
                .addOption(OptionType.INTEGER, "amount", "The quantity of items to give (must be positive)", true);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Get channel ID to determine which server
        // (Reply is already deferred by CommandManager)
        String channelId = event.getChannel().getId();
        System.out.println("[DEBUG] Command executed in channel: " + channelId);
        System.out.println("[DEBUG] Mapped channels: " + ChannelMapper.getMappedChannels());
        ServerConfig serverConfig = ChannelMapper.getServerForChannel(channelId);

        if (serverConfig == null) {
            System.out.println("[DEBUG] Server config is NULL for channel: " + channelId);
            event.getHook().sendMessageEmbeds(createErrorEmbed(
                "This command can only be used in server-specific channels (Fantasy, Vale, or Azerite)."
            )).queue();
            return;
        }

        String playerName = event.getOption("player").getAsString();
        int itemId = event.getOption("itemid").getAsInt();
        int amount = event.getOption("amount").getAsInt();

        // Validate input
        if (amount <= 0) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Amount must be greater than 0")).queue();
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
