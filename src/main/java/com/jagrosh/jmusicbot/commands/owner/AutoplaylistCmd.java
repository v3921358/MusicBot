/*
 * Copyright 2016 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.OtherUtil;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class AutoPlayListCmd extends OwnerCommand {
    private final Bot bot;

    public AutoPlayListCmd(Bot bot) {
        this.bot = bot;
        this.guildOnly = true;
        this.name = "autoPlaylist";
        this.arguments = "<名稱|NONE>";
        this.help = "設定伺服器的預設播放列表";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    public void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + " 請提供播放列表名稱或輸入 NONE");
            return;
        }
        if (event.getArgs().equalsIgnoreCase("none")) {
            Settings settings = event.getClient().getSettingsFor(event.getGuild());
            settings.setDefaultPlaylist(null);
            event.reply(event.getClient().getSuccess() + " 已清除 **" + event.getGuild().getName() + "** 的預設播放列表");
            return;
        }
        String pname = event.getArgs().replaceAll("\\s+", "_");
        if (bot.getPlaylistLoader().getPlaylist(pname) == null) {
            event.reply(event.getClient().getError() + " 找不到 `" + pname + ".txt`！");
        } else {
            Settings settings = event.getClient().getSettingsFor(event.getGuild());
            settings.setDefaultPlaylist(pname);
            event.reply(event.getClient().getSuccess() + " **" + event.getGuild().getName() + "** 的預設播放列表已設定為 `" + pname + "`");
        }
    }
}
