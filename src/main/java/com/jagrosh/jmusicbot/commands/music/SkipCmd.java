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
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.OtherUtil;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SkipCmd extends MusicCommand {
    public SkipCmd(Bot bot) {
        super(bot);
        this.name = "skip";
        this.help = "投票跳過目前播放的歌曲";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true; // 需要機器人在語音頻道
        this.bePlaying = true;   // 需要正在播放音樂
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        RequestMetadata rm = handler.getRequestMetadata();
        double skipRatio = bot.getSettingsManager().getSettings(event.getGuild()).getSkipRatio();
        if (skipRatio == -1) {
            skipRatio = bot.getConfig().getSkipRatio();
        }
        if (event.getAuthor().getIdLong() == rm.getOwner() || skipRatio == 0) {
            event.reply(event.getClient().getSuccess() + " 已跳過 **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**");
            handler.getPlayer().stopTrack();
        } else {
            int listeners = (int) event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> !m.getUser().isBot() && !m.getVoiceState().isDeafened()).count();
            String msg;
            if (handler.getVotes().contains(event.getAuthor().getId()))
                msg = event.getClient().getWarning() + " 你已經對此歌曲投過票 `[";
            else {
                msg = event.getClient().getSuccess() + " 你對此歌曲投票跳過 `[";
                handler.getVotes().add(event.getAuthor().getId());
            }
            int skippers = (int) event.getSelfMember().getVoiceState().getChannel().getMembers().stream()
                    .filter(m -> handler.getVotes().contains(m.getUser().getId())).count();
            int required = (int) Math.ceil(listeners * skipRatio);
            msg += skippers + " 票, 共需 " + required + "/" + listeners + " 票]`";
            if (skippers >= required) {
                msg += "\n" + event.getClient().getSuccess() + " 已跳過 **" + handler.getPlayer().getPlayingTrack().getInfo().title
                        + "** " + (rm.getOwner() == 0L ? "(自動播放)" : "(由 **" + FormatUtil.formatUsername(rm.user) + "** 點播)");
                handler.getPlayer().stopTrack();
            }
            event.reply(msg);
        }
    }

}
