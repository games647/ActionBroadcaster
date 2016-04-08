package com.github.games647.actionbroadcaster;

import com.github.games647.actionbroadcaster.commands.ListCommand;
import com.github.games647.actionbroadcaster.commands.AddCommand;
import com.github.games647.actionbroadcaster.commands.BroadcastCommand;
import com.github.games647.actionbroadcaster.config.Settings;
import com.github.games647.actionbroadcaster.commands.ReloadCommand;
import com.github.games647.actionbroadcaster.commands.RemoveCommand;
import com.google.inject.Inject;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import me.flibio.updatifier.Updatifier;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

@Updatifier(repoOwner = "games647", repoName = "ActionBroadcaster", version = "0.4.2")
@Plugin(id = "com.github.games647.actionbroadcaster", name = "ActionBroadcaster", version = "0.4.2"
        , url = "https://github.com/games647/ActionBroadcaster"
        , description = "A Sponge minecraft server plugin to create automated messages "
                + "that will be printed into the action chat slot.")
public class ActionBroadcaster {

    //disappear time from an action message in seconds which is default in minecraft
    private static final int DISAPPEAR_TIME = 2;

    private final PluginContainer pluginContainer;
    private final Logger logger;
    private final Game game;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private File defaultConfigFile;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    private Settings configuration;

    @Inject
    public ActionBroadcaster(Logger logger, PluginContainer pluginContainer, Game game) {
        this.logger = logger;
        this.pluginContainer = pluginContainer;
        this.game = game;
    }

    @Listener //During this state, the plugin gets ready for initialization. Logger and config
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        configuration = new Settings(configManager, defaultConfigFile, this);
        configuration.load();
    }

    @Listener //During this state, the plugin should finish any work needed in order to be functional. Commands register + events
    public void onInit(GameInitializationEvent initEvent) {
        //register commands
        CommandManager commandDispatcher = game.getCommandManager();

        CommandSpec reloadCommand = CommandSpec.builder()
                .permission(pluginContainer.getUnqualifiedId() + ".reload")
                .description(Text.of(TextColors.RED, "Reloads the entire plugin"))
                .executor(new ReloadCommand(this))
                .build();

        CommandSpec listCommand = CommandSpec.builder()
                .permission(pluginContainer.getUnqualifiedId() + ".list")
                .description(Text.of(TextColors.RED, "Lists all messages"))
                .executor(new ListCommand(this))
                .build();

        CommandSpec addCommand = CommandSpec.builder()
                .permission(pluginContainer.getUnqualifiedId() + ".add")
                .description(Text.of(TextColors.RED, "Adds a new message"))
                .executor(new AddCommand(this))
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("message")))
                .build();

        CommandSpec removeCommand = CommandSpec.builder()
                .permission(pluginContainer.getUnqualifiedId() + ".remove")
                .description(Text.of(TextColors.RED, "Removes a message from the queued broadcast list"))
                .executor(new RemoveCommand(this))
                .arguments(GenericArguments.integer(Text.of("index")))
                .build();

        CommandSpec broadcastCommand = CommandSpec.builder()
                .permission(pluginContainer.getUnqualifiedId() + ".broadcast")
                .description(Text.of(TextColors.RED, "Broadcasts a predefined or new message"))
                .executor(new BroadcastCommand(this))
                .arguments(GenericArguments.firstParsing(
                        GenericArguments.integer(Text.of("index")),
                        GenericArguments.remainingJoinedStrings(Text.of("message"))))
                .build();

        commandDispatcher.register(this, CommandSpec.builder()
                .child(reloadCommand, "reload")
                .child(listCommand, "list", "ls")
                .child(addCommand, "add")
                .child(removeCommand, "rem", "remove", "delete")
                .child(broadcastCommand, "broadcast", "announce")
                .build(), pluginContainer.getUnqualifiedId(), "ab");

        //register events
        game.getEventManager().registerListeners(this, new WelcomeListener(this));
    }

    @Listener
    public void onServerStart(GameAboutToStartServerEvent serverAboutToStartEvent) {
        //The server instance exists, but worlds are not yet loaded.
        if (configuration.getConfiguration() != null && configuration.getConfiguration().isEnabled()) {
            game.getScheduler().createTaskBuilder()
                    .execute(new BroadcastTask(this))
                    .name("Action Broadcaster")
                    .interval(configuration.getConfiguration().getInterval(), TimeUnit.SECONDS)
                    .submit(this);
        }
    }

    public void broadcast(Text message, boolean schedule, Collection<Player> receivers) {
        int remainingAppear = configuration.getConfiguration().getAppearanceTime() - DISAPPEAR_TIME;

        if (sendMessageToAll(message, receivers) && schedule
                && remainingAppear > 0 && remainingAppear < configuration.getConfiguration().getInterval()) {
            game.getScheduler().createTaskBuilder()
                    .delay(remainingAppear % DISAPPEAR_TIME, TimeUnit.SECONDS)
                    .interval(DISAPPEAR_TIME, TimeUnit.SECONDS)
                    .execute(new Consumer<Task>() {

                        private int remainingExecutions = Math.floorDiv(remainingAppear, DISAPPEAR_TIME) + 1;

                        @Override
                        public void accept(Task task) {
                            if (!sendMessageToAll(message, receivers) || --remainingExecutions <= 0) {
                                task.cancel();
                            }
                        }
                    }).submit(this);
        }
    }

    public Text translateColorCodes(String rawInput) {
        return TextSerializers.FORMATTING_CODE.deserialize(rawInput);
    }

    public boolean sendMessageToAll(Text message, Collection<Player> receivers) {
        boolean sent = false;
        for (Player player : receivers) {
            //you cannot send action messages with message sink
            if (player.hasPermission(pluginContainer.getUnqualifiedId() + ".receive")) {
                player.sendMessage(ChatTypes.ACTION_BAR, message);
                sent = true;
            }
        }

        return sent;
    }

    public Settings getConfigManager() {
        return configuration;
    }

    public PluginContainer getContainer() {
        return pluginContainer;
    }

    public Logger getLogger() {
        return logger;
    }

    public Game getGame() {
        return game;
    }
}
