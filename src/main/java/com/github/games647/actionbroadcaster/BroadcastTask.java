package com.github.games647.actionbroadcaster;

import java.util.List;
import java.util.Random;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.chat.ChatTypes;

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

        if (plugin.getConfigManager().getConfiguration().isRandom()) {
            currentIndex = new Random().nextInt(messages.size() - 1);
        } else if (currentIndex >= messages.size()) {
            //we reached the end
            currentIndex = 0;
        }

        String message = messages.get(currentIndex);
        //you cannot send action messages with message sink
        for (Player player : plugin.getGame().getServer().getOnlinePlayers()) {
            if (player.hasPermission(plugin.getContainer().getId() + ".receive")) {
                player.sendMessage(ChatTypes.ACTION_BAR, message);
            }
        }
    }
}
