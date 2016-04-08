package com.github.games647.actionbroadcaster.commands;

import com.github.games647.actionbroadcaster.ActionBroadcaster;

import java.util.List;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class AddCommand implements CommandExecutor {

    private final ActionBroadcaster plugin;

    public AddCommand(ActionBroadcaster plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        //we require it on registration
        String message = args.<String>getOne("message").get();

        List<String> messages = plugin.getConfigManager().getConfiguration().getMessages();
        messages.add(message);

        plugin.getConfigManager().save();
        source.sendMessage(Text.of(TextColors.DARK_GREEN, "Added following message: "));
        source.sendMessage(plugin.translateColorCodes(message));

        return CommandResult.builder().successCount(1).build();
    }
}
