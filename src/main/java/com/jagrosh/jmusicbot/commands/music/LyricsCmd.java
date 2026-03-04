/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * 您不可在未遵守此授權條款的情況下使用本檔案。
 * 您可以在以下網址取得授權內容：
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * 除非法律要求或另有書面約定，
 * 否則依照授權條款分發的軟體皆以「原樣」提供，
 * 不附帶任何明示或默示的擔保或條件。
 * 詳細內容請參閱授權條款。
 */
package com.jagrosh.jmusicbot.commands.music;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jlyrics.LyricsClient;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class LyricsCmd extends MusicCommand {
    private final LyricsClient client = new LyricsClient();

    public LyricsCmd(Bot bot) {
        super(bot);
        this.name = "lyrics";
        this.arguments = "[歌曲名稱]";
        this.help = "顯示歌曲的歌詞";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void doCommand(CommandEvent event) {
        String title;
        if (event.getArgs().isEmpty()) {
            AudioHandler sendingHandler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            if (sendingHandler.isMusicPlaying(event.getJDA()))
                title = sendingHandler.getPlayer().getPlayingTrack().getInfo().title;
            else {
                event.reply(event.getClient().getError() + "必須有音樂正在播放才能使用這個指令！");
                return;
            }
        } else
            title = event.getArgs();

        event.getChannel().sendTyping().queue();

        client.getLyrics(title).thenAccept(lyrics ->
        {
            if (lyrics == null) {
                event.reply(event.getClient().getError() + "找不到 `" + title + "` 的歌詞！"
                        + (event.getArgs().isEmpty() ? " 你可以嘗試手動輸入歌曲名稱（`lyrics [歌曲名稱]`）" : ""));
                return;
            }

            EmbedBuilder eb = new EmbedBuilder()
                    .setAuthor(lyrics.getAuthor())
                    .setColor(event.getSelfMember().getColor())
                    .setTitle(lyrics.getTitle(), lyrics.getURL());

            if (lyrics.getContent().length() > 15000) {
                event.reply(event.getClient().getWarning() + "找到了 `" + title + "` 的歌詞，但內容可能不正確：" + lyrics.getURL());
            } else if (lyrics.getContent().length() > 2000) {
                String content = lyrics.getContent().trim();
                while (content.length() > 2000) {
                    int index = content.lastIndexOf("\n\n", 2000);
                    if (index == -1)
                        index = content.lastIndexOf("\n", 2000);
                    if (index == -1)
                        index = content.lastIndexOf(" ", 2000);
                    if (index == -1)
                        index = 2000;

                    event.reply(eb.setDescription(content.substring(0, index).trim()).build());
                    content = content.substring(index).trim();
                    eb.setAuthor(null).setTitle(null, null);
                }
                event.reply(eb.setDescription(content).build());
            } else
                event.reply(eb.setDescription(lyrics.getContent()).build());
        });
    }
}
