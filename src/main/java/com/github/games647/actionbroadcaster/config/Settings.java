package com.github.games647.actionbroadcaster.config;

import com.github.games647.actionbroadcaster.ActionBroadcaster;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

public class Settings {

    private final Path configFile;

    private final ActionBroadcaster plugin;

    private ObjectMapper<Config>.BoundInstance configMapper;
    private CommentedConfigurationNode rootNode;

    public Settings(ActionBroadcaster plugin, Path defaultConfigFile) {
        this.plugin = plugin;
        this.configFile = defaultConfigFile;

        try {
            configMapper = ObjectMapper.forClass(Config.class).bindToNew();
        } catch (ObjectMappingException objMappingExc) {
            plugin.getLogger().error("Invalid plugin structure", objMappingExc);
        }
    }

    public void load() {
        if (Files.notExists(configFile)) {
            try {
                Files.createDirectories(configFile.getParent());
                Files.createFile(configFile);
            } catch (IOException ioExc) {
                plugin.getLogger().error("Error creating a new config file", ioExc);
                return;
            }
        }

        HoconConfigurationLoader configManager = HoconConfigurationLoader.builder().setPath(configFile).build();
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
                HoconConfigurationLoader.builder().setPath(configFile).build().save(rootNode);
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
