/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetNameCmd extends OwnerCommand {
    public SetNameCmd(Bot bot) {
        this.name = "setName";
        this.help = "設定機器人的名稱";
        this.arguments = "<名稱>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event) {
        try {
            String oldname = event.getSelfUser().getName();
            event.getSelfUser().getManager().setName(event.getArgs()).complete(false);
            event.reply(event.getClient().getSuccess() + " 名稱已從 `" + oldname + "` 更改為 `" + event.getArgs() + "`");
        } catch (RateLimitedException e) {
            event.reply(event.getClient().getError() + " 名稱每小時只能更改兩次！");
        } catch (Exception e) {
            event.reply(event.getClient().getError() + " 無效的名稱！");
        }
    }
}
