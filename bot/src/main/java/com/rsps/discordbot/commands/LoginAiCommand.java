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
 * Command to spawn AI bots in the game
 */
public class LoginAiCommand implements Command {

    private final BotConfig botConfig;

    public LoginAiCommand(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("loginai", "Spawn AI bots in the game")
                .addOption(OptionType.INTEGER, "amount", "Number of AI bots to spawn (max: 50)", true);
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

        int amount = event.getOption("amount").getAsInt();

        // Validate amount
        if (amount <= 0) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Amount must be a positive integer.")).queue();
            return;
        }

        if (amount > 50) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Amount cannot exceed 50 AI bots at once.")).queue();
            return;
        }

        // Create client and execute command
        GameServerClient client = new GameServerClient(serverConfig.getUrl(), serverConfig.getApiKey() != null ? serverConfig.getApiKey() : botConfig.getApiKey());

        try {
            Map<String, Object> result = client.loginAi(amount);

            // Extract data from response
            int spawned = result.get("spawned") != null ? ((Number) result.get("spawned")).intValue() : 0;
            int skipped = result.get("skipped") != null ? ((Number) result.get("skipped")).intValue() : 0;
            int totalAiBots = result.get("totalAiBots") != null ? ((Number) result.get("totalAiBots")).intValue() : 0;

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("AI Bots Spawned")
                    .setColor(Color.GREEN)
                    .addField("Server", serverConfig.getName(), true)
                    .addField("Requested", String.valueOf(amount), true)
                    .addField("Spawned", String.valueOf(spawned), true)
                    .addField("Skipped (Already Online)", String.valueOf(skipped), true)
                    .addField("Total AI Bots Online", String.valueOf(totalAiBots), true)
                    .setFooter("Executed by " + event.getUser().getName());

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to spawn AI bots: " + e.getMessage())).queue();
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
