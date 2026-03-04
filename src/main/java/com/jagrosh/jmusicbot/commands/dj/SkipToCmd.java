/*
 * 版權所有 2016 John Grosh <john.a.grosh@gmail.com>。
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
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.utils.OtherUtil;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SkipToCmd extends DJCommand {
    public SkipToCmd(Bot bot) {
        super(bot);
        this.name = "skipTo";
        this.help = "跳到指定的歌曲";
        this.arguments = "<位置>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true; // 需要正在播放音樂
    }

    @Override
    public void doCommand(CommandEvent event) {
        int index = 0;
        try {
            index = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException e) {
            event.reply(event.getClient().getError() + " `" + event.getArgs() + "` 不是有效的整數！");
            return;
        }
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (index < 1 || index > handler.getQueue().size()) {
            event.reply(event.getClient().getError() + " 位置必須是 1 到 " + handler.getQueue().size() + " 之間的有效整數！");
            return;
        }
        handler.getQueue().skip(index - 1);
        event.reply(event.getClient().getSuccess() + " 已跳到 **" + handler.getQueue().get(0).getTrack().getInfo().title + "**");
        handler.getPlayer().stopTrack();
    }
}
