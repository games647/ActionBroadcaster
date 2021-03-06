package com.github.games647.actionbroadcaster;

import com.github.games647.actionbroadcaster.config.Config;
import com.github.games647.actionbroadcaster.config.Settings;
import com.google.inject.Inject;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.Text;

public class BroadcastTask implements Runnable {

    private final ActionBroadcaster plugin;
    private final Settings settings;
    private int currentIndex;

    @Inject
    BroadcastTask(ActionBroadcaster plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public void run() {
        Config config = settings.getConfiguration();
        List<String> messages = config.getMessages();
        int minPlayers = config.getMinPlayers();
        if (messages.isEmpty() || minPlayers > Sponge.getServer().getOnlinePlayers().size()) {
            return;
        }

        currentIndex++;
        if (config.isRandom()) {
            currentIndex = ThreadLocalRandom.current().nextInt(messages.size());
        } else if (currentIndex >= messages.size()) {
            //we reached the end
            currentIndex = 0;
        }

        Text message = plugin.translateColorCodes(messages.get(currentIndex));
        plugin.broadcast(message, true, Sponge.getServer().getOnlinePlayers());
    }
}
