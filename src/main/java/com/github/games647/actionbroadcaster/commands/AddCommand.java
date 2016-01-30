package com.github.games647.actionbroadcaster.commands;

import com.github.games647.actionbroadcaster.ActionBroadcaster;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class AddCommand implements CommandCallable {

    private final ActionBroadcaster plugin;

    public AddCommand(ActionBroadcaster plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(CommandSource source, String arg) throws CommandException {
        List<String> messages = plugin.getConfigManager().getConfiguration().getMessages();
        messages.add(arg);

        plugin.getConfigManager().save();
        source.sendMessage(Text.of(TextColors.DARK_GREEN, "Added following message: "));
        source.sendMessage(plugin.translateColorCodes(arg));

        return CommandResult.builder().successCount(1).build();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return Collections.emptyList();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission(plugin.getContainer().getId() + ".add");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of(TextColors.RED, TextStyles.NONE, "Adds a new message"));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.of(Text.of(TextColors.RED, TextStyles.NONE, "Adds a new message"));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of();
    }
}
