package com.github.games647.actionbroadcaster.commands;

import com.github.games647.actionbroadcaster.BroadcastTask;
import com.github.games647.actionbroadcaster.ActionBroadcaster;
import com.google.common.base.Optional;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.spongepowered.api.service.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.command.CommandCallable;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;

public class ReloadCommand implements CommandCallable {

    private final ActionBroadcaster plugin;

    public ReloadCommand(ActionBroadcaster plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(CommandSource source, String arg) throws CommandException {
        plugin.getConfigManager().load();

        //cancel all tasks and schedule new ones
        for (Task task : plugin.getGame().getScheduler().getScheduledTasks(plugin)) {
            task.cancel();
        }

        if (plugin.getConfigManager().getConfiguration().isEnabled()) {
            plugin.getGame().getScheduler().getTaskBuilder()
                    .execute(new BroadcastTask(plugin))
                    .name("Action Broadcaster")
                    .interval(plugin.getConfigManager().getConfiguration().getInterval(), TimeUnit.SECONDS)
                    .submit(this);
        }

        source.sendMessage(Texts.builder("Reloaded the plugin").color(TextColors.DARK_RED).build());
        return CommandResult.success();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return Collections.emptyList();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission(plugin.getContainer().getId() + ".reload");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.of(Texts.of(TextColors.RED, TextStyles.NONE, "Reloads the entire plugin"));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.of(Texts.of(TextColors.RED, TextStyles.NONE, "Reloads the entire plugin"));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Texts.of();
    }
}
