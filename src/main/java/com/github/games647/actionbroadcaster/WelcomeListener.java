package com.github.games647.actionbroadcaster;

import com.google.common.collect.Sets;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

public class WelcomeListener {

    private final ActionBroadcaster plugin;

    public WelcomeListener(ActionBroadcaster plugin) {
        this.plugin = plugin;
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join joinEvent) {
        Player receiver = joinEvent.getTargetEntity();

        String welcomeMessage = plugin.getConfigManager().getConfiguration().getWelcomeMessage();
        if (!welcomeMessage.isEmpty()) {
            Text coloredMessage = plugin.translateColorCodes(welcomeMessage);
            plugin.broadcast(coloredMessage, true, Sets.newHashSet(receiver));
        }
    }
}
