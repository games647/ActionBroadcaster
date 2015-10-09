package com.github.games647.actionbroadcaster.commands;

import com.github.games647.actionbroadcaster.ActionBroadcaster;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

public class RemoveCommand implements CommandCallable {

    private final ActionBroadcaster plugin;

    public RemoveCommand(ActionBroadcaster plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(CommandSource source, String arg) throws CommandException {
        String[] args = arg.split(" ");
        if (args.length >= 1) {
            String index = args[0];
            Integer indexParsed = Ints.tryParse(index);
            if (indexParsed == null) {
                source.sendMessage(Texts.of(TextColors.DARK_RED, index + " is not a index number"));
            } else {
                int intValue = indexParsed;

                List<String> messages = plugin.getConfigManager().getConfiguration().getMessages();
                if (indexParsed > messages.size()) {
                    source.sendMessage(Texts.of(TextColors.DARK_RED
                            , index + '/' + messages.size()
                                  + " Number is higher than the available messages"));
                } else {
                    String removedMessage = messages.remove(intValue - 1);
                    plugin.getConfigManager().save();

                    source.sendMessage(Texts.of(TextColors.DARK_GREEN, "Removed the following message"));
                    source.sendMessage(Texts.of(removedMessage));
                    return CommandResult.builder().successCount(1).build();
                }
            }
        }

        return CommandResult.success();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        List<String> messages = plugin.getConfigManager().getConfiguration().getMessages();

        List<String> suggestions = Lists.newArrayList();
        for (int i = 1; i <= messages.size(); i++) {
            suggestions.add(Integer.toString(i));
        }

        return suggestions;
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission(plugin.getContainer().getId() + ".remove");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.of(Texts.of(TextColors.RED, TextStyles.NONE, "Reloads the entire plugin"));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.empty();
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Texts.of();
    }
}
