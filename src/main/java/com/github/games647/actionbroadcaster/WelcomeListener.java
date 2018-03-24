package com.github.games647.actionbroadcaster;

import com.github.games647.actionbroadcaster.config.Settings;
import com.google.inject.Inject;

import java.util.Collections;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

public class WelcomeListener {

    private final ActionBroadcaster plugin;
    private final Settings settings;

    @Inject
    public WelcomeListener(ActionBroadcaster plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join joinEvent) {
        Player receiver = joinEvent.getTargetEntity();

        String welcomeMessage = settings.getConfiguration().getWelcomeMessage();
        if (!welcomeMessage.isEmpty()) {
            Text coloredMessage = plugin.translateColorCodes(welcomeMessage);
            plugin.broadcast(coloredMessage, true, Collections.singleton(receiver));
        }
    }
}
