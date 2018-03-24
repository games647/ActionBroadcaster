package com.github.games647.actionbroadcaster.commands;

import com.github.games647.actionbroadcaster.ActionBroadcaster;
import com.github.games647.actionbroadcaster.PomData;
import com.github.games647.actionbroadcaster.config.Settings;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

import javax.print.attribute.TextSyntax;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public class ListCommand implements CommandExecutor {

    private final ActionBroadcaster plugin;
    private final Settings settings;

    @Inject
    ListCommand(ActionBroadcaster plugin, Settings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext args) throws CommandException {
        List<String> messages = settings.getConfiguration().getMessages();

        List<Text> contents = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            String message = messages.get(i);
            contents.add(buildMessage(i, message));
        }

        if (contents.isEmpty()) {
            source.sendMessage(Text.of(TextColors.RED, "There are no messages"));
            return CommandResult.success();
        }

        PaginationList.builder()
                .title(Text
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

    private Text buildMessage(int index, String text) {
        String textWithOutLineBreaks = text.replace("\n", "/newline");

        Text line = plugin.translateColorCodes(textWithOutLineBreaks);
        if (textWithOutLineBreaks.length() > 45) {
            line = Text.builder().append(plugin.translateColorCodes(textWithOutLineBreaks.substring(0, 45)))
                    .append(Text.of(TextStyles.RESET, TextColors.YELLOW, " ...")).build();
        }

        return Text.builder().append(line)
                .onHover(TextActions.showText(Text.of(text)))
                .onClick(TextActions
                        .runCommand('/' + PomData.ARTIFACT_ID + " broadcast " + (index + 1)))
                //do not add colors to the text message in order to show the actual results
                .append(Text
                        .builder(" ✖")
                        .color(TextColors.DARK_RED)
                        .onClick(TextActions
                                .runCommand('/' + PomData.ARTIFACT_ID + " remove " + (index + 1)))
                        .onHover(TextActions
                                .showText(Text.of(TextColors.RED, TextStyles.ITALIC, "Removes this message")))
                        .build())
                .build();
    }
}
