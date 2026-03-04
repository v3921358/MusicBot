package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.settings.RepeatMode;
import com.jagrosh.jmusicbot.settings.Settings;
import net.dv8tion.jda.api.interactions.InteractionContextType;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class RepeatCmd extends DJCommand {
    public RepeatCmd(Bot bot) {
        super(bot);
        this.name = "repeat";
        this.help = "播放完畢後自動重新加入音樂到播放隊列";
        this.arguments = "[off|all|single]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};
    }

    // 覆寫 MusicCommand 的 execute，因為我們不在乎此指令在哪裡使用
    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();
        RepeatMode value;
        Settings settings = event.getClient().getSettingsFor(event.getGuild());

        if (args.isEmpty()) {
            if (settings.getRepeatMode() == RepeatMode.OFF)
                value = RepeatMode.ALL;
            else
                value = RepeatMode.OFF;
        } else if (args.equalsIgnoreCase("false") || args.equalsIgnoreCase("off")) {
            value = RepeatMode.OFF;
        } else if (args.equalsIgnoreCase("true") || args.equalsIgnoreCase("on") || args.equalsIgnoreCase("all")) {
            value = RepeatMode.ALL;
        } else if (args.equalsIgnoreCase("one") || args.equalsIgnoreCase("single")) {
            value = RepeatMode.SINGLE;
        } else {
            event.reply(event.getClient().getError() + "有效選項為 `off`、`all` 或 `single`（或留空以在 `off` 與 `all` 之間切換）");
            return;
        }

        settings.setRepeatMode(value);
        event.reply(event.getClient().getSuccess() + "重複模式已設定為 `" + value.getUserFriendlyName() + "`");
    }

    @Override
    public void doCommand(CommandEvent event) { /* 故意留空 */ }
}
