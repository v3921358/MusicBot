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
package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class RemoveCmd extends MusicCommand {
    public RemoveCmd(Bot bot) {
        super(bot);
        this.name = "remove";
        this.help = "從播放隊列中移除歌曲";
        this.arguments = "<位置|ALL>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true; // 需要機器人在語音頻道
        this.bePlaying = true;   // 需要正在播放音樂
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getQueue().isEmpty()) {
            event.reply(event.getClient().getError() + "播放隊列中沒有任何歌曲！");
            return;
        }
        if (event.getArgs().equalsIgnoreCase("all")) {
            int count = handler.getQueue().removeAll(event.getAuthor().getIdLong());
            if (count == 0)
                event.reply(event.getClient().getWarning() + "你在播放隊列中沒有任何歌曲！");
            else
                event.reply(event.getClient().getSuccess() + "成功移除了你添加的 " + count + " 首歌曲。");
            return;
        }
        int pos;
        try {
            pos = Integer.parseInt(event.getArgs());
        } catch (NumberFormatException e) {
            pos = 0;
        }
        if (pos < 1 || pos > handler.getQueue().size()) {
            event.reply(event.getClient().getError() + "位置必須是介於 1 到 " + handler.getQueue().size() + " 的有效整數！");
            return;
        }
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        boolean isDJ = event.getMember().hasPermission(Permission.MANAGE_SERVER);
        if (!isDJ)
            isDJ = event.getMember().getRoles().contains(settings.getRole(event.getGuild()));
        QueuedTrack qt = handler.getQueue().get(pos - 1);
        if (qt.getIdentifier() == event.getAuthor().getIdLong()) {
            handler.getQueue().remove(pos - 1);
            event.reply(event.getClient().getSuccess() + "已從播放隊列中移除 **" + qt.getTrack().getInfo().title + "**");
        } else if (isDJ) {
            handler.getQueue().remove(pos - 1);
            User u;
            try {
                u = event.getJDA().getUserById(qt.getIdentifier());
            } catch (Exception e) {
                u = null;
            }
            event.reply(event.getClient().getSuccess() + "已從播放隊列中移除 **" + qt.getTrack().getInfo().title
                    + "** (原始請求者: " + (u == null ? "某人" : "**" + u.getName() + "**") + ")");
        } else {
            event.reply(event.getClient().getError() + "你無法移除 **" + qt.getTrack().getInfo().title + "**，因為你沒有添加它！");
        }
    }
}
