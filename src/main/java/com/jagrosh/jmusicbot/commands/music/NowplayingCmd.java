/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * 您不可在未遵守授權條款的情況下使用本檔案。
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
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.MusicCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class NowPlayingCmd extends MusicCommand
{
    public NowPlayingCmd(Bot bot)
    {
        super(bot);
        this.name = "nowPlaying";
        this.help = "顯示當前正在播放的歌曲";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void doCommand(CommandEvent event)
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        MessageCreateData data = handler.getNowPlaying(event.getJDA());
        if(data == null)
        {
            event.getChannel().sendMessage(handler.getNoMusicPlaying(event.getJDA())).queue();
            bot.getNowplayingHandler().clearLastNPMessage(event.getGuild());
        }
        else
        {
            event.getChannel().sendMessage(data).queue(msg -> bot.getNowplayingHandler().setLastNPMessage(msg));
        }
    }
}
