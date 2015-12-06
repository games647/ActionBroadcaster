package com.github.games647.actionbroadcaster;

import com.github.games647.actionbroadcaster.commands.ListCommand;
import com.github.games647.actionbroadcaster.commands.AddCommand;
import com.github.games647.actionbroadcaster.commands.BroadcastCommand;
import com.github.games647.actionbroadcaster.commands.VersionCommand;
import com.github.games647.actionbroadcaster.config.Settings;
import com.github.games647.actionbroadcaster.commands.ReloadCommand;
import com.github.games647.actionbroadcaster.commands.RemoveCommand;
import com.google.inject.Inject;

import java.io.File;
import java.util.concurrent.TimeUnit;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Texts;

@Plugin(id = "actionbroadcaster", name = "ActionBroadcaster", version = "0.1.7")
public class ActionBroadcaster {

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
        logger.info("Loading {} v{}", pluginContainer.getName(), pluginContainer.getVersion());

        configuration = new Settings(configManager, defaultConfigFile, this);
        configuration.load();
    }

    @Listener //During this state, the plugin should finish any work needed in order to be functional. Commands register + events
    public void onInit(GameInitializationEvent initEvent) {
        //register commands
        CommandManager commandDispatcher = initEvent.getGame().getCommandManager();
        CommandSpec mainCommands = CommandSpec.builder()
                .executor(new VersionCommand(this))
                .child(new ReloadCommand(this), "reload", "r")
                .child(new ListCommand(this), "list", "ls")
                .child(new AddCommand(this), "add")
                .child(new RemoveCommand(this), "rem", "remove", "delete")
                .child(new BroadcastCommand(this), "broadcast", "announce")
                .build();
        commandDispatcher.register(this, mainCommands, pluginContainer.getId(), "ab");
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

    public String translateColorCodes(String rawInput) {
        return Texts.replaceCodes(rawInput, '&');
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
