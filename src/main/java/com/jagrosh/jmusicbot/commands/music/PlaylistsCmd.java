/*
 * 版權所有 2018 John Grosh <john.a.grosh@gmail.com>。
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
package com.jagrosh.jmusicbot.commands.music;

import java.util.List;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import net.dv8tion.jda.api.interactions.InteractionContextType;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class PlaylistsCmd extends MusicCommand {
    public PlaylistsCmd(Bot bot) {
        super(bot);
        this.name = "playLists";
        this.help = "顯示可用的播放清單";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD}; // 只能在伺服器使用
        this.beListening = false; // 不需要機器人在語音頻道中
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (!bot.getPlaylistLoader().folderExists())
            bot.getPlaylistLoader().createFolder();
        if (!bot.getPlaylistLoader().folderExists()) {
            event.reply(event.getClient().getWarning() + " 播放清單資料夾不存在，且無法建立！");
            return;
        }
        List<String> list = bot.getPlaylistLoader().getPlaylistNames();
        if (list == null)
            event.reply(event.getClient().getError() + " 無法載入可用的播放清單！");
        else if (list.isEmpty())
            event.reply(event.getClient().getWarning() + " 播放清單資料夾中沒有任何播放清單！");
        else {
            StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " 可用的播放清單如下：\n");
            list.forEach(str -> builder.append("`").append(str).append("` "));
            builder.append("\n輸入 `").append(event.getClient().getTextualPrefix()).append("play playlist <名稱>` 播放指定播放清單");
            event.reply(builder.toString());
        }
    }
}
