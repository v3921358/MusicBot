/*
 * 版權所有 2020 John Grosh <john.a.grosh@gmail.com>。
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
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Whew., Inc.
 */
public class SeekCmd extends MusicCommand {
    private final static Logger LOG = LoggerFactory.getLogger("Seeking");

    public SeekCmd(Bot bot) {
        super(bot);
        this.name = "seek";
        this.help = "快轉或倒退目前播放的歌曲";
        this.arguments = "[+ | -] <HH:MM:SS | MM:SS | SS>|<0h0m0s | 0m0s | 0s>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true; // 需要機器人在語音頻道
        this.bePlaying = true;   // 需要正在播放音樂
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        AudioTrack playingTrack = handler.getPlayer().getPlayingTrack();
        if (!playingTrack.isSeekable()) {
            event.reply(event.getClient().getError() + "此歌曲無法快轉或倒退。");
            return;
        }

        if (!DJCommand.checkDJPermission(event) && playingTrack.getUserData(RequestMetadata.class).getOwner() != event.getAuthor().getIdLong()) {
            event.reply(event.getClient().getError() + "你無法快轉或倒退 **" + playingTrack.getInfo().title + "**，因為你沒有添加它！");
            return;
        }

        String args = event.getArgs();
        TimeUtil.SeekTime seekTime = TimeUtil.parseTime(args);
        if (seekTime == null) {
            event.reply(event.getClient().getError() + "快轉格式無效！預期格式: " + arguments + "\n範例: `1:02:23` `+1:10` `-90`, `1h10m`, `+90s`");
            return;
        }

        long currentPosition = playingTrack.getPosition();
        long trackDuration = playingTrack.getDuration();

        long seekMilliseconds = seekTime.relative ? currentPosition + seekTime.milliseconds : seekTime.milliseconds;
        if (seekMilliseconds > trackDuration) {
            event.reply(event.getClient().getError() + "無法快轉到 `" + TimeUtil.formatTime(seekMilliseconds) + "`，因為目前歌曲長度為 `" + TimeUtil.formatTime(trackDuration) + "`！");
            return;
        }

        try {
            playingTrack.setPosition(seekMilliseconds);
        } catch (Exception e) {
            event.reply(event.getClient().getError() + "嘗試快轉時發生錯誤: " + e.getMessage());
            LOG.warn("無法快轉歌曲 " + playingTrack.getIdentifier(), e);
            return;
        }
        event.reply(event.getClient().getSuccess() + "已成功快轉至 `" + TimeUtil.formatTime(playingTrack.getPosition()) + "/" + TimeUtil.formatTime(playingTrack.getDuration()) + "`！");
    }
}
