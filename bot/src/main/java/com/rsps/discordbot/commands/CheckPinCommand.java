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
 * Command to check a player's bank pin
 */
public class CheckPinCommand implements Command {

    private final BotConfig botConfig;

    public CheckPinCommand(BotConfig botConfig) {
        this.botConfig = botConfig;
    }

    @Override
    public CommandData getCommandData() {
        return Commands.slash("checkpin", "Check a player's bank pin")
                .addOption(OptionType.STRING, "username", "The username of the player", true);
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

        String username = event.getOption("username").getAsString();

        // Create client and execute command
        GameServerClient client = new GameServerClient(serverConfig.getUrl(), botConfig.getApiKey());

        try {
            Map<String, Object> response = client.checkPin(username);

            String pin = (String) response.get("pin");
            String playerUsername = (String) response.get("username");

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Bank Pin Information")
                    .setColor(Color.CYAN)
                    .addField("Server", serverConfig.getName(), true)
                    .addField("Player", playerUsername != null ? playerUsername : username, true)
                    .addField("Bank Pin", pin != null ? pin : "No pin set", false)
                    .setFooter("Executed by " + event.getUser().getName());

            event.getHook().sendMessageEmbeds(embed.build()).queue();

        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(createErrorEmbed("Failed to check pin: " + e.getMessage())).queue();
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
