package com.github.games647.actionbroadcaster.commands;

import com.github.games647.actionbroadcaster.ActionBroadcaster;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

public class VersionCommand implements CommandExecutor {

    private final ActionBroadcaster plugin;

    public VersionCommand(ActionBroadcaster plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        source.sendMessage(Texts
                .builder(plugin.getContainer().getName() + " v")
                .color(TextColors.YELLOW)
                .append(Texts
                        .builder(plugin.getContainer().getVersion())
                        .color(TextColors.DARK_BLUE)
                        .build())
                .build());

        return CommandResult.success();
    }
}
