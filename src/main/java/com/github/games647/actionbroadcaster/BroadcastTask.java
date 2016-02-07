package com.github.games647.actionbroadcaster;

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
        List<String> messages = plugin.getConfigManager().getConfiguration().getMessages();
        if (messages.isEmpty()) {
            return;
        }

        currentIndex++;
        if (plugin.getConfigManager().getConfiguration().isRandom()) {
            currentIndex = new Random().nextInt(messages.size());
        } else if (currentIndex >= messages.size()) {
            //we reached the end
            currentIndex = 0;
        }

        Text message = plugin.translateColorCodes(messages.get(currentIndex));
        plugin.broadcast(message, true);
    }
}
