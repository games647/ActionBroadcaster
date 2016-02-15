package com.github.games647.actionbroadcaster;

import com.github.games647.actionbroadcaster.config.Config;

import java.util.List;
import java.util.Random;

import org.spongepowered.api.text.Text;

public class BroadcastTask implements Runnable {

    private final ActionBroadcaster plugin;

    private int currentIndex;

    public BroadcastTask(ActionBroadcaster plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Config config = plugin.getConfigManager().getConfiguration();
        List<String> messages = config.getMessages();
        int minPlayers = config.getMinPlayers();
        if (messages.isEmpty() || minPlayers > plugin.getGame().getServer().getOnlinePlayers().size()) {
            return;
        }

        currentIndex++;
        if (config.isRandom()) {
            currentIndex = new Random().nextInt(messages.size());
        } else if (currentIndex >= messages.size()) {
            //we reached the end
            currentIndex = 0;
        }

        Text message = plugin.translateColorCodes(messages.get(currentIndex));
        plugin.broadcast(message, true, plugin.getGame().getServer().getOnlinePlayers());
    }
}
