/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * 除非符合授權條款，否則不得使用此檔案。
 * 你可以從以下網址取得授權：
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * 除非法律要求或書面同意，否則依授權提供的程式碼是「原樣提供」，不附任何保證。
 */
package com.jagrosh.jmusicbot.commands.admin;

import java.util.List;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetVoiceChannelCmd extends AdminCommand {
    public SetVoiceChannelCmd(Bot bot) {
        this.name = "setVC";
        this.help = "設定播放音樂的語音頻道";
        this.arguments = "<頻道名稱|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + " 請提供語音頻道名稱或輸入 NONE");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().equalsIgnoreCase("none")) {
            s.setVoiceChannel(null);
            event.reply(event.getClient().getSuccess() + " 現在音樂可以在任何頻道播放");
        } else {
            List<VoiceChannel> list = event.getGuild().getVoiceChannels().stream()
                    .filter(c -> c.getName().toLowerCase().contains(event.getArgs().toLowerCase()))
                    .toList();
            if (list.isEmpty())
                event.reply(event.getClient().getWarning() + " 找不到與 \"" + event.getArgs() + "\" 匹配的語音頻道");
            else if (list.size() > 1)
                event.reply(event.getClient().getWarning() + FormatUtil.listOfVChannels(list, event.getArgs()));
            else {
                s.setVoiceChannel(list.get(0));
                event.reply(event.getClient().getSuccess() + " 現在音樂只能在 " + list.get(0).getAsMention() + " 播放");
            }
        }
    }
}
