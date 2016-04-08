package com.github.games647.actionbroadcaster.commands;

import com.github.games647.actionbroadcaster.BroadcastTask;
import com.github.games647.actionbroadcaster.ActionBroadcaster;

import java.util.concurrent.TimeUnit;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class ReloadCommand implements CommandExecutor {

    private final ActionBroadcaster plugin;

    public ReloadCommand(ActionBroadcaster plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        plugin.getConfigManager().load();

        //cancel all tasks and schedule new ones
        for (Task task : plugin.getGame().getScheduler().getScheduledTasks(plugin)) {
            task.cancel();
        }

        if (plugin.getConfigManager().getConfiguration().isEnabled()) {
            plugin.getGame().getScheduler().createTaskBuilder()
                    .execute(new BroadcastTask(plugin))
                    .name("Action Broadcaster")
                    .interval(plugin.getConfigManager().getConfiguration().getInterval(), TimeUnit.SECONDS)
                    .submit(plugin);
        }

        source.sendMessage(Text.builder("Reloaded the plugin").color(TextColors.DARK_RED).build());
        return CommandResult.success();
    }
}
