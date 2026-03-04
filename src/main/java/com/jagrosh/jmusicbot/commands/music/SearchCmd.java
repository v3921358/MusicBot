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

import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SearchCmd extends MusicCommand {
    protected String searchPrefix = "ytsearch:";
    private final String searchingEmoji;

    public SearchCmd(Bot bot) {
        super(bot);
        this.searchingEmoji = bot.getConfig().getSearching();
        this.name = "search";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.arguments = "<查詢關鍵字>";
        this.help = "搜尋 Youtube 中提供的關鍵字";
        this.beListening = true;
        this.bePlaying = false;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + "請輸入查詢關鍵字。");
            return;
        }
        event.reply(searchingEmoji + " 搜尋中... `[" + event.getArgs() + "]`",
                m -> bot.getPlayerManager().loadItemOrdered(event.getGuild(), searchPrefix + event.getArgs(), new ResultHandler(m, event)));
    }

    private class ResultHandler implements AudioLoadResultHandler {
        private final Message m;
        private final CommandEvent event;

        private ResultHandler(Message m, CommandEvent event) {
            this.m = m;
            this.event = event;
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            if (bot.getConfig().isTooLong(track)) {
                m.editMessage(FormatUtil.filter(event.getClient().getWarning() + " 此歌曲 (**" + track.getInfo().title + "**) 長度超過允許最大值: `"
                        + TimeUtil.formatTime(track.getDuration()) + "` > `" + bot.getConfig().getMaxTime() + "`")).queue();
                return;
            }
            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            int pos = handler.addTrack(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event))) + 1;
            m.editMessage(FormatUtil.filter(event.getClient().getSuccess() + " 已加入 **" + track.getInfo().title
                    + "** (`" + TimeUtil.formatTime(track.getDuration()) + "`) " + (pos == 0 ? "立即播放"
                    : " 加入播放隊列第 " + pos + " 位"))).queue();
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("search_result");
            menuBuilder.setPlaceholder("請選擇一首歌曲");

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 10 && i < playlist.getTracks().size(); i++) {
                AudioTrack track = playlist.getTracks().get(i);
                sb.append("`[").append(TimeUtil.formatTime(track.getDuration())).append("]`[**").append(track.getInfo().title).append("**](").append(track.getInfo().uri).append(")\n");

                String title = track.getInfo().title;
                if (title.length() > 100) title = title.substring(0, 97) + "...";
                menuBuilder.addOption(title, String.valueOf(i), TimeUtil.formatTime(track.getDuration()));
            }
            menuBuilder.addOption("取消", "cancel");

            EmbedBuilder embedBuilder = new EmbedBuilder()
                    .setColor(event.getSelfMember().getColors().getPrimary())
                    .setDescription(sb.toString());

            m.editMessage(FormatUtil.filter(event.getClient().getSuccess() + " `" + event.getArgs() + "` 的搜尋結果:"))
                    .setEmbeds(embedBuilder.build())
                    .setComponents(ActionRow.of(menuBuilder.build()))
                    .queue(edited -> {
                        bot.getWaiter().waitForEvent(
                                StringSelectInteractionEvent.class,
                                e -> e.getMessageIdLong() == edited.getIdLong() && e.getUser().equals(event.getAuthor()),
                                e -> {
                                    String value = e.getValues().get(0);
                                    if (value.equals("cancel")) {
                                        e.editMessage("已取消搜尋。").setEmbeds().setComponents().queue();
                                        return;
                                    }
                                    int index = Integer.parseInt(value);
                                    AudioTrack track = playlist.getTracks().get(index);
                                    if (bot.getConfig().isTooLong(track)) {
                                        e.editMessage(event.getClient().getWarning() + "此歌曲 (**" + track.getInfo().title + "**) 長度超過允許最大值: `"
                                                + TimeUtil.formatTime(track.getDuration()) + "` > `" + bot.getConfig().getMaxTime() + "`").setEmbeds().setComponents().queue();
                                        return;
                                    }
                                    AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
                                    int pos = handler.addTrack(new QueuedTrack(track, RequestMetadata.fromResultHandler(track, event))) + 1;
                                    e.editMessage(event.getClient().getSuccess() + "已加入 **" + FormatUtil.filter(track.getInfo().title)
                                            + "** (`" + TimeUtil.formatTime(track.getDuration()) + "`) " + (pos == 0 ? "立即播放"
                                            : " 加入播放隊列第 " + pos + " 位")).setEmbeds().setComponents().queue();
                                },
                                1, TimeUnit.MINUTES,
                                () -> m.editMessageComponents(Collections.emptyList()).queue(s -> {}, f -> {})
                        );
                    });
        }

        @Override
        public void noMatches() {
            m.editMessage(FormatUtil.filter(event.getClient().getWarning() + " 沒有找到符合 `" + event.getArgs() + "` 的結果。")).queue();
        }

        @Override
        public void loadFailed(FriendlyException throwable) {
            if (throwable.severity == Severity.COMMON)
                m.editMessage(event.getClient().getError() + " 載入失敗: " + throwable.getMessage()).queue();
            else
                m.editMessage(event.getClient().getError() + " 載入歌曲時發生錯誤。").queue();
        }
    }
}