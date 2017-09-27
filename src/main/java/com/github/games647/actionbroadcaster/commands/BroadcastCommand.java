package com.github.games647.actionbroadcaster.commands;

import com.github.games647.actionbroadcaster.ActionBroadcaster;

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class BroadcastCommand implements CommandExecutor {

    private final ActionBroadcaster plugin;

    public BroadcastCommand(ActionBroadcaster plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        Optional<Integer> optionalIndex = args.getOne("index");

        if (optionalIndex.isPresent()) {
            int index = optionalIndex.get();
            List<String> messages = plugin.getConfigManager().getConfiguration().getMessages();
            if (index > messages.size()) {
                source.sendMessage(Text.of(TextColors.DARK_RED, index + "/" + messages.size()
                        + " Number is higher than the available messages"));
                return CommandResult.empty();
            }

            String message = messages.get(index - 1);
            Text coloredMessage = plugin.translateColorCodes(message);
            plugin.broadcast(coloredMessage, true, Sponge.getServer().getOnlinePlayers());

            source.sendMessage(Text.of(TextColors.DARK_GREEN, "Broadcasted message: "));
            source.sendMessage(Text.of(plugin.translateColorCodes(message)));
            return CommandResult.success();
        }

        //either the index or this message is forced
        String message = args.<String>getOne("message").get();
        Text coloredMessage = plugin.translateColorCodes(message);
        plugin.broadcast(coloredMessage, true, Sponge.getServer().getOnlinePlayers());

        source.sendMessage(Text.of(TextColors.DARK_GREEN, "Broadcasted message"));
        return CommandResult
                .builder()
                .affectedEntities(Sponge.getServer().getOnlinePlayers().size())
                .build();
    }
}
