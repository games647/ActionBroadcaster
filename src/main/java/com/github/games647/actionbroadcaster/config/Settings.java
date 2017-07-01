package com.github.games647.actionbroadcaster.config;

import com.github.games647.actionbroadcaster.ActionBroadcaster;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class Settings {

    private final ConfigurationLoader<CommentedConfigurationNode> configManager;
    private final Path defaultConfigFile;

    private final ActionBroadcaster plugin;

    private ObjectMapper<Config>.BoundInstance configMapper;
    private CommentedConfigurationNode rootNode;

    public Settings(ConfigurationLoader<CommentedConfigurationNode> configManager
            , Path defaultConfigFile, ActionBroadcaster plugin) {
        this.configManager = configManager;
        this.plugin = plugin;
        this.defaultConfigFile = defaultConfigFile;

        try {
            configMapper = ObjectMapper.forClass(Config.class).bindToNew();
        } catch (ObjectMappingException objMappingExc) {
            plugin.getLogger().error("Invalid plugin structure", objMappingExc);
        }
    }

    public void load() {
        if (Files.notExists(defaultConfigFile)) {
            try {
                Files.createFile(defaultConfigFile);
            } catch (IOException ioExc) {
                plugin.getLogger().error("Error creating a new config file", ioExc);
                return;
            }
        }

        rootNode = configManager.createEmptyNode();
        if (configMapper != null) {
            try {
                rootNode = configManager.load();

                //load the config into the object
                configMapper.populate(rootNode);

                //add missing default values
                configMapper.serialize(rootNode);
                configManager.save(rootNode);
            } catch (ObjectMappingException objMappingExc) {
                plugin.getLogger().error("Error loading the configuration", objMappingExc);
            } catch (IOException ioExc) {
                plugin.getLogger().error("Error saving the default configuration", ioExc);
            }
        }
    }

    public void save() {
        if (configMapper != null && rootNode != null) {
            try {
                configMapper.serialize(rootNode);
                configManager.save(rootNode);
            } catch (ObjectMappingException objMappingExc) {
                plugin.getLogger().error("Error serialize the configuration", objMappingExc);
            } catch (IOException ioExc) {
                plugin.getLogger().error("Error saving the configuration", ioExc);
            }
        }
    }

    public Config getConfiguration() {
        if (configMapper == null) {
            return null;
        }

        return configMapper.getInstance();
    }
}
