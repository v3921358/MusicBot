/*
 * 版權所有 2017 John Grosh <john.a.grosh@gmail.com>。
 *
 * 根據 Apache License, Version 2.0（以下簡稱「授權」）授權使用。
 * 除非遵守授權條款，否則不得使用此檔案。
 * 你可以在以下網址取得授權副本：
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * 除非法律要求或書面同意，否則軟體是「按原樣」提供，
 * 不附任何明示或暗示的保證。
 * 詳細請參閱授權條款。
 */
package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.interactions.InteractionContextType;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetStatusCmd extends OwnerCommand {
    public SetStatusCmd(Bot bot) {
        this.name = "setStatus";
        this.help = "設定機器人的狀態";
        this.arguments = "<status>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM};
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            OnlineStatus status = OnlineStatus.fromKey(event.getArgs());
            if (status == OnlineStatus.UNKNOWN) {
                event.reply(event.getClient().getError() + "請輸入以下其中一個狀態：`ONLINE`, `IDLE`, `DND`, `INVISIBLE`");
            } else {
                event.getJDA().getPresence().setStatus(status);
                event.reply(event.getClient().getSuccess() + "已將狀態設為 `" + status.getKey().toUpperCase() + "`");
            }
        } catch (Exception e) {
            event.reply(event.getClient().getError() + " 無法設定狀態！");
        }
    }
}
