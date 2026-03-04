/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * 您不可在未遵循此授權條款的情況下使用本檔案。
 * 您可以在以下網址取得授權內容：
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * 除非法律要求或另有書面約定，
 * 否則依照授權條款分發的軟體皆以「原樣」提供，
 * 不附帶任何明示或默示的擔保或條件。
 * 詳細內容請參閱授權條款。
 */
package com.jagrosh.jmusicbot.commands.admin;

import java.util.List;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetTextChannelCmd extends AdminCommand {
    public SetTextChannelCmd(Bot bot) {
        this.name = "setTC";
        this.help = "設定音樂指令可以使用的文字頻道";
        this.arguments = "<頻道名稱|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + " 請輸入文字頻道名稱，或輸入 NONE");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().equalsIgnoreCase("none")) {
            s.setTextChannel(null);
            event.reply(event.getClient().getSuccess() + " 音樂指令現在可以在任何頻道中使用");
        } else {
            List<TextChannel> list = event.getGuild().getTextChannels().stream()
                    .filter(c -> c.getName().toLowerCase().contains(event.getArgs().toLowerCase()))
                    .toList();
            if (list.isEmpty())
                event.reply(event.getClient().getWarning() + " 找不到符合「" + event.getArgs() + "」的文字頻道");
            else if (list.size() > 1)
                event.reply(event.getClient().getWarning() + FormatUtil.listOfTChannels(list, event.getArgs()));
            else {
                s.setTextChannel(list.get(0));
                event.reply(event.getClient().getSuccess() + " 音樂指令現在只能在 <#" + list.get(0).getId() + "> 中使用");
            }
        }
    }

}
