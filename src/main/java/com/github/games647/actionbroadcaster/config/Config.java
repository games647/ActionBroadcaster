package com.github.games647.actionbroadcaster.config;

import com.google.common.collect.Lists;

import java.util.List;

import ninja.leaping.configurate.objectmapping.Setting;

public class Config {

    @Setting(value = "enabled", comment = "Disable the entire broadcast functionality")
    private boolean enabled = true;

    @Setting(value = "interval", comment = "Interval in seconds to wait for the next message")
    private int interval = 320;

    @Setting(value = "random", comment = "Should the message be selected for displaying in random order")
    private boolean random = true;

    @Setting(value = "messages"
            , comment = "All messages which will be displayed. Use § as color char for colored messages")
    private List<String> messages = Lists.newArrayList("§aExample Message");

    public boolean isEnabled() {
        return enabled;
    }

    public int getInterval() {
        return interval;
    }

    public boolean isRandom() {
        return random;
    }

    public List<String> getMessages() {
        return messages;
    }
}
