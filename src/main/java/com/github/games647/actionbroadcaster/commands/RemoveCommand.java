package com.github.games647.actionbroadcaster.commands;

import com.github.games647.actionbroadcaster.ActionBroadcaster;
import com.github.games647.actionbroadcaster.config.Settings;
import com.google.inject.Inject;

import java.util.List;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class RemoveCommand implements CommandExecutor {

    private final ActionBroadcaster plugin;
    private final Settings settings;

    @Inject
    RemoveCommand(ActionBroadcaster plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        int index = args.<Integer>getOne("index").get();

        List<String> messages = settings.getConfiguration().getMessages();
        if (index > messages.size()) {
            source.sendMessage(Text.of(TextColors.DARK_RED, index + '/' + messages.size()
                    + " Number is higher than the available messages"));
        } else {
            Text removedMessage = plugin.translateColorCodes(messages.remove(index - 1));
            settings.save();

            source.sendMessage(Text.of(TextColors.DARK_GREEN, "Removed the following message"));
            source.sendMessage(removedMessage);
            return CommandResult.builder().successCount(1).build();
        }

        return CommandResult.success();
    }

//    @Override
//    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
//        List<String> messages = plugin.getConfigManager().getConfiguration().getMessages();
//
//        List<String> suggestions = Lists.newArrayList();
//        for (int i = 1; i <= messages.size(); i++) {
//            suggestions.add(Integer.toString(i));
//        }
//
//        return suggestions;
//    }
}
