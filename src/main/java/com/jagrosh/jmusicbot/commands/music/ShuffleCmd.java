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

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.OtherUtil;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class ShuffleCmd extends MusicCommand {
    public ShuffleCmd(Bot bot) {
        super(bot);
        this.name = "shuffle";
        this.help = "隨機播放你添加的歌曲";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true; // 需要機器人在語音頻道
        this.bePlaying = true;   // 需要正在播放音樂
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        int s = handler.getQueue().shuffle(event.getAuthor().getIdLong());
        switch (s) {
            case 0:
                event.reply(event.getClient().getError() + "你在播放隊列中沒有任何歌曲可隨機播放！");
                break;
            case 1:
                event.reply(event.getClient().getWarning() + "你在播放隊列中只有一首歌曲！");
                break;
            default:
                event.reply(event.getClient().getSuccess() + "你已成功隨機播放你添加的 " + s + " 首歌曲。");
                break;
        }
    }

}
