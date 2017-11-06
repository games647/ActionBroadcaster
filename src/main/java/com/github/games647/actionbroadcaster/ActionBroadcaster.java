package com.github.games647.actionbroadcaster;

import com.github.games647.actionbroadcaster.commands.AddCommand;
import com.github.games647.actionbroadcaster.commands.BroadcastCommand;
import com.github.games647.actionbroadcaster.commands.ListCommand;
import com.github.games647.actionbroadcaster.commands.ReloadCommand;
import com.github.games647.actionbroadcaster.commands.RemoveCommand;
import com.github.games647.actionbroadcaster.config.Settings;
import com.google.inject.Inject;
import com.google.inject.Injector;

import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandManager;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import static org.spongepowered.api.command.args.GenericArguments.firstParsing;
import static org.spongepowered.api.command.args.GenericArguments.integer;
import static org.spongepowered.api.command.args.GenericArguments.remainingJoinedStrings;
import static org.spongepowered.api.text.Text.of;

@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION
        , url = PomData.URL, description = PomData.DESCRIPTION)
public class ActionBroadcaster {

    //disappear time from an action message in seconds which is default in minecraft
    private static final int DISAPPEAR_TIME = 2;

    private final Injector injector;
    private final Settings configuration;
    
    @Inject
    ActionBroadcaster(Injector injector, Logger logger, @DefaultConfig(sharedRoot = true) Path configFile) {
        this.configuration = new Settings(logger, configFile);
        this.injector = injector.createChildInjector(binder -> binder.bind(Settings.class).toInstance(configuration));
    }

    @Listener //During this state, the plugin gets ready for initialization. Logger and config
    public void onPreInit(GamePreInitializationEvent preInitEvent) {
        configuration.load();
    }

    @Listener
    //During this state, the plugin should finish any work needed in order to be functional. Commands register + events
    public void onInit(GameInitializationEvent initEvent) {
        //register commands
        CommandManager commandDispatcher = Sponge.getCommandManager();

        System.out.println(injector.getBinding(Settings.class).getProvider().get());
        CommandSpec reloadCommand = CommandSpec.builder()
                .permission(PomData.ARTIFACT_ID + ".reload")
                .description(of(TextColors.RED, "Reloads the entire plugin"))
                .executor(injector.getInstance(ReloadCommand.class))
                .build();

        CommandSpec listCommand = CommandSpec.builder()
                .permission(PomData.ARTIFACT_ID + ".list")
                .description(of(TextColors.RED, "Lists all messages"))
                .executor(injector.getInstance(ListCommand.class))
                .build();

        CommandSpec addCommand = CommandSpec.builder()
                .permission(PomData.ARTIFACT_ID + ".add")
                .description(of(TextColors.RED, "Adds a new message"))
                .executor(injector.getInstance(AddCommand.class))
                .arguments(remainingJoinedStrings(of("message")))
                .build();

        CommandSpec removeCommand = CommandSpec.builder()
                .permission(PomData.ARTIFACT_ID + ".remove")
                .description(of(TextColors.RED, "Removes a message from the queued broadcast list"))
                .executor(injector.getInstance(RemoveCommand.class))
                .arguments(integer(of("index")))
                .build();

        CommandSpec broadcastCommand = CommandSpec.builder()
                .permission(PomData.ARTIFACT_ID + ".broadcast")
                .description(of(TextColors.RED, "Broadcasts a predefined or new message"))
                .executor(injector.getInstance(BroadcastCommand.class))
                .arguments(firstParsing(
                        integer(of("index")),
                        remainingJoinedStrings(of("message"))))
                .build();

        commandDispatcher.register(this, CommandSpec.builder()
                .child(reloadCommand, "reload")
                .child(listCommand, "list", "ls")
                .child(addCommand, "add")
                .child(removeCommand, "rem", "remove", "delete")
                .child(broadcastCommand, "broadcast", "announce")
                .build(), PomData.ARTIFACT_ID, "ab");

        //register events
        Sponge.getEventManager().registerListeners(this, injector.getInstance(WelcomeListener.class));
    }

    @Listener
    public void onServerStart(GameAboutToStartServerEvent serverAboutToStartEvent) {
        //The server instance exists, but worlds are not yet loaded.
        if (configuration.getConfiguration() != null && configuration.getConfiguration().isEnabled()) {
            System.out.println(injector.getBinding(Settings.class).getProvider().get());
            Task.builder()
                    .execute(injector.getInstance(BroadcastTask.class))
                    .name("Action Broadcaster")
                    .interval(configuration.getConfiguration().getInterval(), TimeUnit.SECONDS)
                    .submit(this);
        }
    }

    public void broadcast(Text message, boolean schedule, Collection<Player> receivers) {
        int remainingAppear = configuration.getConfiguration().getAppearanceTime() - DISAPPEAR_TIME;

        if (sendMessageToAll(message, receivers) && schedule
                && remainingAppear > 0 && remainingAppear < configuration.getConfiguration().getInterval()) {
            Task.builder()
                    .delay(remainingAppear % DISAPPEAR_TIME, TimeUnit.SECONDS)
                    .interval(DISAPPEAR_TIME, TimeUnit.SECONDS)
                    .execute(new Consumer<Task>() {

                        private int remainingExecutions = Math.floorDiv(remainingAppear, DISAPPEAR_TIME) + 1;

                        @Override
                        public void accept(Task task) {
                            if (!sendMessageToAll(message, receivers) || --remainingExecutions <= 0) {
                                task.cancel();
                            }
                        }
                    }).submit(this);
        }
    }

    public Text translateColorCodes(String rawInput) {
        return TextSerializers.FORMATTING_CODE.deserialize(rawInput);
    }

    private boolean sendMessageToAll(Text message, Collection<Player> receivers) {
        //you cannot send action messages with message sink
        long received = receivers.stream()
                .filter(player -> player.hasPermission(PomData.ARTIFACT_ID + ".receive"))
                .peek(player -> {
                    if (configuration.getConfiguration().isChat()) {
                        player.sendMessage(ChatTypes.CHAT, message);
                    } else {
                        player.sendMessage(ChatTypes.ACTION_BAR, message);
                    }
                })
                .count();

        return received > 0;
    }
}
