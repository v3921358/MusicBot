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
public class PauseCmd extends DJCommand {
    public PauseCmd(Bot bot) {
        super(bot);
        this.name = "pause";
        this.help = "暫停當前播放的歌曲";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();

        if (handler.getPlayer().isPaused()) {
            // 播放器已經暫停
            event.reply(event.getClient().getWarning() + "播放器已經暫停！使用 `" + event.getClient().getPrefix() + "play` 指令來恢復播放！");
            return;
        }

        // 暫停播放器
        handler.getPlayer().setPaused(true);
        event.reply(event.getClient().getSuccess() + "已暫停 **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**。輸入 `" + event.getClient().getPrefix() + "play` 來恢復播放！");
    }
}
