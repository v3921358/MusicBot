package com.jagrosh.jmusicbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.OtherUtil;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class SkipRatioCmd extends AdminCommand {
    public SkipRatioCmd(Bot bot) {
        this.name = "setSkip";
        this.help = "設定伺服器專屬的跳過歌曲百分比";
        this.arguments = "<0 - 100>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            int val = Integer.parseInt(event.getArgs().endsWith("%") ? event.getArgs().substring(0, event.getArgs().length() - 1) : event.getArgs());
            if (val < 0 || val > 100) {
                event.reply(event.getClient().getError() + "提供的數值必須介於 0 到 100 之間！");
                return;
            }
            Settings s = event.getClient().getSettingsFor(event.getGuild());
            s.setSkipRatio(val / 100.0);
            event.reply(event.getClient().getSuccess() + "跳過歌曲百分比已設定為 `" + val + "%`，作用於伺服器 *" + event.getGuild().getName() + "* 的聆聽者。");
        } catch (NumberFormatException ex) {
            event.reply(event.getClient().getError() + "請提供一個介於 0 到 100 的整數（預設值為 55）。此數字代表需要投票跳過歌曲的聆聽者百分比。");
        }
    }
}
