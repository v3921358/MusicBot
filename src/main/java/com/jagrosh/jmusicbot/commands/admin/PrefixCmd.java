package com.jagrosh.jmusicbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.OtherUtil;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class PrefixCmd extends AdminCommand {
    public PrefixCmd(Bot bot) {
        this.name = "prefix";
        this.help = "設定伺服器專屬前綴";
        this.arguments = "<前綴|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + "請輸入一個前綴或 NONE");
            return;
        }

        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().equalsIgnoreCase("none")) {
            s.setPrefix(null);
            event.reply(event.getClient().getSuccess() + "前綴已清除。");
        } else {
            s.setPrefix(event.getArgs());
            event.reply(event.getClient().getSuccess() + "伺服器 *" + event.getGuild().getName() + "* 的自訂前綴已設定為 `" + event.getArgs() + "`");
        }
    }
}
