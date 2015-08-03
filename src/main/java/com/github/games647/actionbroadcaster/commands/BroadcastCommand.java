package com.github.games647.actionbroadcaster.commands;

import com.github.games647.actionbroadcaster.ActionBroadcaster;
import com.google.common.base.Optional;
import com.google.common.primitives.Ints;

import java.util.Collections;
import java.util.List;
import org.spongepowered.api.entity.player.Player;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

public class BroadcastCommand implements CommandCallable {

    private final ActionBroadcaster plugin;

    public BroadcastCommand(ActionBroadcaster plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(CommandSource source, String arg) throws CommandException {
        String[] args = arg.split(" ");
        if (args.length >= 1) {
            Integer firstArg = Ints.tryParse(args[0]);
            if (firstArg != null) {
                int index = firstArg.intValue();
                List<String> messages = plugin.getConfigManager().getConfiguration().getMessages();
                if (index > messages.size()) {
                    source.sendMessage(Texts.of(TextColors.DARK_RED, index + "/" + messages.size()
                            + " Number is higher than the available messages"));
                } else {
                    String message = messages.get(index - 1);
                    for (Player onlinePlayer : plugin.getGame().getServer().getOnlinePlayers()) {
                        onlinePlayer.sendMessage(ChatTypes.ACTION_BAR, message);
                    }

                    source.sendMessage(Texts.of(TextColors.DARK_GREEN, "Broadcasted message: "));
                    source.sendMessage(Texts.of(message));
                }

                return CommandResult.success();
            }
        }

        for (Player onlinePlayer : plugin.getGame().getServer().getOnlinePlayers()) {
            onlinePlayer.sendMessage(ChatTypes.ACTION_BAR, plugin.translateColorCodes(arg));
        }

        source.sendMessage(Texts.of(TextColors.DARK_GREEN, "Broadcasted message"));
        return CommandResult
                .builder()
                .affectedEntities(plugin.getGame().getServer().getOnlinePlayers().size())
                .build();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return Collections.emptyList();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission(plugin.getContainer().getId() + ".broadcast");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.of(Texts.of(TextColors.RED, TextStyles.NONE, "Broadcasts a predefined or new message"));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.of(Texts.of(TextColors.RED, TextStyles.NONE, "Broadcasts a predefined or new message"));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Texts.of();
    }
}
