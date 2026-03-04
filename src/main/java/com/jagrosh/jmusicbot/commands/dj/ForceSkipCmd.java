/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 您不可在未遵守此授權的情況下使用本檔案。
 * 您可在以下網址取得授權條款：
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * 除非適用法律要求或書面同意，
 * 否則依照授權條款分發的軟體是按「原樣」提供，
 * 不含任何明示或默示的擔保或條件。
 * 授權條款中已詳細描述相關的權利與限制。
 */
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.RequestMetadata;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.OtherUtil;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class ForceSkipCmd extends DJCommand {
    public ForceSkipCmd(Bot bot) {
        super(bot);
        this.name = "forceSkip";
        this.help = "強制跳過目前正在播放的歌曲";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        RequestMetadata rm = handler.getRequestMetadata();
        event.reply(event.getClient().getSuccess() + " 已跳過 **"
                + handler.getPlayer().getPlayingTrack().getInfo().title
                + "** "
                + (rm.getOwner() == 0L ? "(自動播放)"
                : "(由 **" + FormatUtil.formatUsername(rm.user) + "** 要求)"));
        handler.getPlayer().stopTrack();
    }
}
