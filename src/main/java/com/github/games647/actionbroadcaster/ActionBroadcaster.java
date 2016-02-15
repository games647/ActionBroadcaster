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
import org.spongepowered.api.text.serializer.FormattingCodeTextSerializer;
import org.spongepowered.api.text.serializer.TextSerializers;

@Updatifier(repoOwner = "games647", repoName = "ActionBroadcaster", version = "0.4")
@Plugin(id = "actionbroadcaster", name = "ActionBroadcaster", version = "0.4")
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
        CommandSpec mainCommands = CommandSpec.builder()
                .child(new ReloadCommand(this), "reload", "r")
                .child(new ListCommand(this), "list", "ls")
                .child(new AddCommand(this), "add")
                .child(new RemoveCommand(this), "rem", "remove", "delete")
                .child(new BroadcastCommand(this), "broadcast", "announce")
                .build();
        commandDispatcher.register(this, mainCommands, pluginContainer.getId(), "ab");

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
        FormattingCodeTextSerializer legacy = TextSerializers.formattingCode('&');
        return legacy.deserialize(rawInput);
    }

    public boolean sendMessageToAll(Text message, Collection<Player> receivers) {
        boolean sent = false;
        for (Player player : receivers) {
            //you cannot send action messages with message sink
            if (player.hasPermission(pluginContainer.getId() + ".receive")) {
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
