/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
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

import java.io.IOException;
import java.io.InputStream;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.api.entities.Icon;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetAvatarCmd extends OwnerCommand {
    public SetAvatarCmd(Bot bot) {
        this.name = "setAvatar";
        this.help = "設定機器人的頭像";
        this.arguments = "<網址>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        String url;
        if (event.getArgs().isEmpty())
            if (!event.getMessage().getAttachments().isEmpty() && event.getMessage().getAttachments().get(0).isImage())
                url = event.getMessage().getAttachments().get(0).getUrl();
            else
                url = null;
        else
            url = event.getArgs();

        InputStream s = OtherUtil.imageFromUrl(url);
        if (s == null) {
            event.reply(event.getClient().getError() + " 網址無效或缺失");
        } else {
            try {
                event.getSelfUser().getManager().setAvatar(Icon.from(s)).queue(
                        v -> event.reply(event.getClient().getSuccess() + " 頭像已成功更改。"),
                        t -> event.reply(event.getClient().getError() + " 設定頭像失敗。"));
            } catch (IOException e) {
                event.reply(event.getClient().getError() + " 無法從提供的網址載入圖片。");
            }
        }
    }
}
