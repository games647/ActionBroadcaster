package com.github.games647.actionbroadcaster.commands;

import com.github.games647.actionbroadcaster.ActionBroadcaster;
import com.github.games647.actionbroadcaster.BroadcastTask;
import com.github.games647.actionbroadcaster.config.Settings;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.util.concurrent.TimeUnit;

import org.spongepowered.api.Sponge;
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
    private final Injector injector;
    private final Settings settings;

    @Inject
    ReloadCommand(ActionBroadcaster plugin, Settings settings, Injector injector) {
        this.plugin = plugin;
        this.settings = settings;
        this.injector = injector;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        settings.load();

        //cancel all tasks and schedule new ones
        Sponge.getScheduler().getScheduledTasks(plugin).forEach(Task::cancel);

        if (settings.getConfiguration().isEnabled()) {
            Task.builder()
                    .execute(injector.getInstance(BroadcastTask.class))
                    .name("Action Broadcaster")
                    .interval(settings.getConfiguration().getInterval(), TimeUnit.SECONDS)
                    .submit(plugin);
        }

        source.sendMessage(Text.builder("Reloaded the plugin").color(TextColors.DARK_RED).build());
        return CommandResult.success();
    }
}
