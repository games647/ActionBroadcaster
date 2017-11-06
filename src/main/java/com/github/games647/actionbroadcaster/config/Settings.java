package com.github.games647.actionbroadcaster.config;

import java.io.IOException;
import java.nio.file.Path;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import org.slf4j.Logger;

public class Settings {

    private final Logger logger;
    private final ConfigurationLoader<CommentedConfigurationNode> configManager;

    private ObjectMapper<Config>.BoundInstance configMapper;
    private CommentedConfigurationNode rootNode;

    public Settings(Logger logger, Path configFile) {
        this.logger = logger;
        this.configManager = HoconConfigurationLoader.builder().setPath(configFile).build();

        try {
            configMapper = ObjectMapper.forClass(Config.class).bindToNew();
        } catch (ObjectMappingException objMappingExc) {
            logger.error("Invalid plugin structure", objMappingExc);
        }
    }

    public void load() {
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
                logger.error("Error loading the configuration", objMappingExc);
            } catch (IOException ioExc) {
                logger.error("Error saving the default configuration", ioExc);
            }
        }
    }

    public void save() {
        if (configMapper != null && rootNode != null) {
            try {
                configMapper.serialize(rootNode);
                configManager.save(rootNode);
            } catch (ObjectMappingException objMappingExc) {
                logger.error("Error serialize the configuration", objMappingExc);
            } catch (IOException ioExc) {
                logger.error("Error saving the configuration", ioExc);
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
