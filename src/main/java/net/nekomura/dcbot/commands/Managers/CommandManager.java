package net.nekomura.dcbot.commands.Managers;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.nekomura.dcbot.Config;
import net.nekomura.dcbot.Enums.ConfigStringData;
import net.nekomura.dcbot.commands.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class CommandManager {
    public CommandManager() {
        addCommand(new RandomAnimeIllustration());
        addCommand(new PixivIllustration());
        addCommand(new Sauce());
        addCommand(new RandomAnimeIllustrationNSFWAble());
        addCommand(new RandomAnimeIllustrationR18());
        addCommand(new Search());
        addCommand(new SearchNSFWAble());
        addCommand(new SearchR18());
    }

    private final List<ICommand> commands = new ArrayList<>();

    private void addCommand(ICommand cmd) {
        boolean nameFound = this.commands.stream().anyMatch((it) -> it.getName().equalsIgnoreCase(cmd.getName()));

        if (nameFound) {
            throw new IllegalArgumentException("Command duplicated.");
        }

        commands.add(cmd);
    }

    @Nullable
    private ICommand getCommand(String keyword) {
        String lower = keyword.toLowerCase();

        for (ICommand cmd: this.commands) {
            if (cmd.getName().equalsIgnoreCase(lower) || cmd.getAliases().stream().anyMatch(it -> it.equalsIgnoreCase(lower))) {
                return cmd;
            }
        }

        return null;
    }

    public void handle(GuildMessageReceivedEvent event) throws Exception {
        try {
            String[] split = event.getMessage().getContentRaw()
                    .replaceFirst("(?i)" + Pattern.quote(Objects.requireNonNull(Config.get(ConfigStringData.PREFIX))), "")
                    .split("\\s+");
            String invoke = split[0].toLowerCase();
            ICommand cmd = this.getCommand(invoke);

            if (cmd != null) {
                event.getChannel().sendTyping().queue();
                List<String> args = Arrays.asList(split).subList(1, split.length);

                CommandContext ctx = new CommandContext(event, args);

                cmd.handle(ctx);
            }
        }catch (Throwable e) {
            event.getChannel().sendMessage("錯誤發生！\r\n```" + e.toString() + "```\r\n請聯繫管理員或稍後重試。").queue();
        }
    }
}