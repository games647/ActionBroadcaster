package com.github.games647.actionbroadcaster.commands;

import com.github.games647.actionbroadcaster.ActionBroadcaster;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.pagination.PaginationList.Builder;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class ListCommand implements CommandCallable {

    private final ActionBroadcaster plugin;

    public ListCommand(ActionBroadcaster plugin) {
        this.plugin = plugin;
    }

    @Override
    public CommandResult process(CommandSource source, String arg) throws CommandException {
        List<String> messages = plugin.getConfigManager().getConfiguration().getMessages();

        List<Text> contents = Lists.newArrayList();
        for (int i = 0; i < messages.size(); i++) {
            String message = messages.get(i);
            contents.add(buildMessage(i, message));
        }

        if (contents.isEmpty()) {
            source.sendMessage(Text.of(TextColors.RED, "There are no messages"));
            return CommandResult.success();
        }


        PaginationService paginationService = plugin.getGame().getServiceManager().provideUnchecked(PaginationService.class);
        Builder builder = paginationService.builder();
        builder.title(Text
                .builder()
                .color(TextColors.DARK_BLUE)
                .append(Text
                        .builder("Messages")
                        .color(TextColors.YELLOW)
                        .build())
                .build())
                .padding(Text.of("="))
                .contents(contents)
                .sendTo(source);

        return CommandResult.success();
    }

    @Override
    public List<String> getSuggestions(CommandSource source, String arguments) throws CommandException {
        return Collections.emptyList();
    }

    @Override
    public boolean testPermission(CommandSource source) {
        return source.hasPermission(plugin.getContainer().getId() + ".list");
    }

    @Override
    public Optional<? extends Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of(TextColors.RED, TextStyles.NONE, "Lists all messages"));
    }

    @Override
    public Optional<? extends Text> getHelp(CommandSource source) {
        return Optional.of(Text.of(TextColors.RED, TextStyles.NONE, "Lists all messages"));
    }

    @Override
    public Text getUsage(CommandSource source) {
        return Text.of();
    }

    private Text buildMessage(int index, String text) {
        String lineText = text;
        if (lineText.length() > 32) {
            lineText = lineText.substring(0, 32) + "...";
        }

        return Text.builder(plugin.translateColorCodes(lineText), "")
                .onHover(TextActions.showText(Text.of(text)))
                //do not add colors to the text message in order to show the actual results
                .append(Text
                        .builder(" ✖")
                        .color(TextColors.DARK_RED)
                        .onClick(TextActions.runCommand('/' + plugin.getContainer().getId() + " remove " + (index + 1)))
                        .onHover(TextActions
                                .showText(Text.of(TextColors.RED, TextStyles.ITALIC, "Removes this message")))
                        .build())
                .build();
    }
}
