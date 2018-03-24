package com.github.games647.actionbroadcaster.config;

import com.google.common.collect.Lists;

import java.util.List;

import ninja.leaping.configurate.objectmapping.Setting;

public class Config {

    @Setting(comment = "Disable the entire broadcast functionality")
    private boolean enabled = true;

    @Setting(comment = "Should the message be broadcasted into chat or into the action bar")
    private boolean chat;

    @Setting(comment = "Interval in seconds to wait for the next message")
    private int interval = 320;

    @Setting(comment = "Should the message be selected for displaying in random order")
    private boolean random = true;

    @Setting(comment = "Specify the minimum number of players for messages to be broadcasted on the server.")
    private int minPlayers = 1;

    @Setting(comment = "Message that will be displayed on join. Set this to an empty text in order to disable it")
    private String welcomeMessage = "";

    @Setting(comment = "All messages which will be displayed. Use & as color char for colored messages")
    private List<String> messages = Lists.newArrayList("&aExample Message");

    @Setting(comment = "How long should the message be displayed in seconds. "
            + "This value have to be higher than 2 (Minecraft Default)")
    private int appearanceTime = 2;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isChat() {
        return chat;
    }

    public int getInterval() {
        return interval;
    }

    public boolean isRandom() {
        return random;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public List<String> getMessages() {
        return messages;
    }

    public int getAppearanceTime() {
        return chat ? 1 :appearanceTime;
    }
}
