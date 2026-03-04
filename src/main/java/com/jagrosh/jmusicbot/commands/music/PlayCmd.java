/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.menu.ButtonMenu;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.playlist.PlaylistLoader.Playlist;
import com.jagrosh.jmusicbot.utils.FormatUtil;

import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.exceptions.PermissionException;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class PlayCmd extends MusicCommand {
    private final static String LOAD = "\uD83D\uDCE5"; // 📥
    private final static String CANCEL = "\uD83D\uDEAB"; // 🚫

    private final String loadingEmoji;

    public PlayCmd(Bot bot) {
        super(bot);
        this.loadingEmoji = "⏳";
        this.name = "play";
        this.arguments = "<標題|網址|子命令>";
        this.help = "播放指定的歌曲";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = true;
        this.bePlaying = false;
        this.children = new Command[]{new PlaylistCmd(bot)};
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty() && event.getMessage().getAttachments().isEmpty()) {
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            if (handler.getPlayer().getPlayingTrack() != null && handler.getPlayer().isPaused()) {
                if (DJCommand.checkDJPermission(event)) {
                    handler.getPlayer().setPaused(false);
                    event.reply(event.getClient().getSuccess() + "已繼續播放 **" + handler.getPlayer().getPlayingTrack().getInfo().title + "**。");
                } else
                    event.reply(event.getClient().getError() + "只有 DJ 才能取消暫停播放器！");
                return;
            }
            StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " 播放指令說明:\n");
            builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" <歌曲名稱>` - 播放 YouTube 的第一筆搜尋結果");
            builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" <網址>` - 播放指定歌曲、播放清單或串流內容");
            for (Command cmd : children)
                builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName()).append(" ").append(cmd.getArguments()).append("` - ").append(cmd.getHelp());
            event.reply(builder.toString());
            return;
        }

        String args = event.getArgs().startsWith("<") && event.getArgs().endsWith(">")
                ? event.getArgs().substring(1, event.getArgs().length() - 1)
                : event.getArgs().isEmpty() ? event.getMessage().getAttachments().get(0).getUrl() : event.getArgs();

        event.reply(loadingEmoji + " 載入中... `[" + args + "]`",
                m -> bot.getPlayerManager().loadItemOrdered(event.getGuild(), args, new ResultHandler(m, event, false)));
    }

    private class ResultHandler implements AudioLoadResultHandler {
        private final Message m;
        private final CommandEvent event;
        private final boolean youtubeSearch;

        private ResultHandler(Message m, CommandEvent event, boolean youtubeSearch) {
            this.m = m;
            this.event = event;
            this.youtubeSearch = youtubeSearch;
        }

        private void loadSingle(AudioTrack track, AudioPlaylist playlist) {
            if (bot.getConfig().isTooLong(track)) {
                m.editMessage(FormatUtil.filter(event.getClient().getWarning()
                        + " 此曲目（**" + track.getInfo().title + "**）長度超過允許的最大值：`"
                        + TimeUtil.formatTime(track.getDuration()) + "` > `"
                        + TimeUtil.formatTime(bot.getConfig().getMaxSeconds() * 1000) + "`")).queue();
                return;
            }
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            int pos = handler.addTrack(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event))) + 1;

            String addMsg = FormatUtil.filter(event.getClient().getSuccess() + " 已加入 **" + track.getInfo().title
                    + "** (`" + TimeUtil.formatTime(track.getDuration()) + "`) "
                    + (pos == 0 ? "開始播放" : "加入隊列於位置 " + pos));

            if (playlist == null || !event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_ADD_REACTION))
                m.editMessage(addMsg).queue();
            else {
                new ButtonMenu.Builder()
                        .setText(addMsg + "\n" + event.getClient().getWarning()
                                + " 此曲目附帶含 **" + playlist.getTracks().size() + "** 首歌曲的播放清單。選擇 "
                                + LOAD + " 以載入播放清單。")
                        .setChoices(LOAD, CANCEL)
                        .setEventWaiter(bot.getWaiter())
                        .setTimeout(30, TimeUnit.SECONDS)
                        .setAction(re ->
                        {
                            if (re.getName().equals(LOAD))
                                m.editMessage(addMsg + "\n"
                                        + event.getClient().getSuccess() + " 已載入 **"
                                        + loadPlaylist(playlist, track) + "** 首額外曲目！").queue();
                            else
                                m.editMessage(addMsg).queue();
                        })
                        .setFinalAction(me ->
                        {
                            try {
                                me.clearReactions().queue();
                            } catch (PermissionException ignore) {}
                        }).build().display(m);
            }
        }

        private int loadPlaylist(AudioPlaylist playlist, AudioTrack exclude) {
            int[] count = {0};
            playlist.getTracks().stream().forEach((track) -> {
                if (!bot.getConfig().isTooLong(track) && !track.equals(exclude)) {
                    AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                    handler.addTrack(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event)));
                    count[0]++;
                }
            });
            return count[0];
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            loadSingle(track, null);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            if (playlist.getTracks().size() == 1 || playlist.isSearchResult()) {
                AudioTrack single = playlist.getSelectedTrack() == null
                        ? playlist.getTracks().get(0) : playlist.getSelectedTrack();
                loadSingle(single, null);
            } else if (playlist.getSelectedTrack() != null) {
                AudioTrack single = playlist.getSelectedTrack();
                loadSingle(single, playlist);
            } else {
                int count = loadPlaylist(playlist, null);
                if (playlist.getTracks().isEmpty()) {
                    m.editMessage(FormatUtil.filter(event.getClient().getWarning()
                            + " 無法載入該播放清單 " + (playlist.getName() == null ? "" : "(**" + playlist.getName() + "**) ")
                            + " 或內容為 0 筆")).queue();
                } else if (count == 0) {
                    m.editMessage(FormatUtil.filter(event.getClient().getWarning()
                            + " 此播放清單 " + (playlist.getName() == null ? "" : "(**" + playlist.getName() + "**) ")
                            + "內所有曲目皆超過最大允許時間（`" + bot.getConfig().getMaxTime() + "`）")).queue();
                } else {
                    m.editMessage(FormatUtil.filter(event.getClient().getSuccess() + " 找到 "
                            + (playlist.getName() == null ? "一個播放清單" : "播放清單 **" + playlist.getName() + "**")
                            + "，共有 `" + playlist.getTracks().size() + "` 首歌曲；已加入隊列！"
                            + (count < playlist.getTracks().size()
                            ? "\n" + event.getClient().getWarning() + " 部分曲目因超過最大允許時間（`"
                            + bot.getConfig().getMaxTime() + "`）已省略。"
                            : ""))).queue();
                }
            }
        }

        @Override
        public void noMatches() {
            if (youtubeSearch)
                m.editMessage(FormatUtil.filter(event.getClient().getWarning()
                        + " 找不到與 `" + event.getArgs() + "` 相符的結果。")).queue();
            else
                bot.getPlayerManager().loadItemOrdered(event.getGuild(),
                        "ytsearch:" + event.getArgs(),
                        new ResultHandler(m, event, true));
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            if (throwable.severity == Severity.COMMON)
                m.editMessage(event.getClient().getError()
                        + " 載入時發生錯誤：" + throwable.getMessage()).queue();
            else
                m.editMessage(event.getClient().getError()
                        + " 載入曲目時發生錯誤。").queue();
        }
    }

    public class PlaylistCmd extends MusicCommand {
        public PlaylistCmd(Bot bot) {
            super(bot);
            this.name = "playList";
            this.aliases = new String[]{"pl"};
            this.arguments = "<name>";
            this.help = "播放指定的播放清單";
            this.beListening = true;
            this.bePlaying = false;
        }

        @Override
        public void doCommand(CommandEvent event) {
            if (event.getArgs().isEmpty()) {
                event.reply(event.getClient().getError() + " 請輸入播放清單名稱。");
                return;
            }
            Playlist playlist = bot.getPlaylistLoader().getPlaylist(event.getArgs());
            if (playlist == null) {
                event.reply(event.getClient().getError() + "找不到 `" + event.getArgs() + ".txt`（位於 Playlists 資料夾）。");
                return;
            }
            event.getChannel().sendMessage(loadingEmoji + " 正在載入播放清單 **"
                            + event.getArgs() + "**... （" + playlist.getItems().size() + " 個項目）")
                    .queue(m ->
                    {
                        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                        playlist.loadTracks(bot.getPlayerManager(),
                                (at) -> handler.addTrack(new QueuedTrack(at, RequestMetadata.fromResultHandler(at, event))),
                                () -> {
                                    StringBuilder builder = new StringBuilder(
                                            playlist.getTracks().isEmpty()
                                                    ? event.getClient().getWarning() + " 無任何曲目被載入！"
                                                    : event.getClient().getSuccess() + " 已載入 **" + playlist.getTracks().size() + "** 首歌曲！");
                                    if (!playlist.getErrors().isEmpty())
                                        builder.append("\n以下曲目載入失敗：");
                                    playlist.getErrors().forEach(err -> builder.append("\n`[")
                                            .append(err.getIndex() + 1).append("]` **")
                                            .append(err.getItem()).append("**: ")
                                            .append(err.getReason()));
                                    String str = builder.toString();
                                    if (str.length() > 2000)
                                        str = str.substring(0, 1994) + " (...)";
                                    m.editMessage(FormatUtil.filter(str)).queue();
                                });
                    });
        }
    }
}
