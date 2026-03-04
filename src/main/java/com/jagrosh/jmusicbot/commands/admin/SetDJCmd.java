/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands.admin;

import java.util.List;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.api.entities.Role;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetDJCmd extends AdminCommand {

    public SetDJCmd(Bot bot) {
        this.name = "setDJ";
        this.help = "設定某些音樂指令的 DJ 身分組";
        this.arguments = "<身分組名稱|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + " 請輸入身分組名稱或 NONE");
            return;
        }
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if (event.getArgs().equalsIgnoreCase("none")) {
            s.setDJRole(null);
            event.reply(event.getClient().getSuccess() + " 已清除 DJ 身分組；只有管理員可以使用 DJ 指令。");
        } else {
            List<Role> list = FinderUtil.findRoles(event.getArgs(), event.getGuild());
            if (list.isEmpty())
                event.reply(event.getClient().getWarning() + " 找不到符合 \"" + event.getArgs() + "\" 的身分組");
            else if (list.size() > 1)
                event.reply(event.getClient().getWarning() + FormatUtil.listOfRoles(list, event.getArgs()));
            else {
                s.setDJRole(list.get(0));
                event.reply(event.getClient().getSuccess() + " 具有 **" + list.get(0).getName() + "** 角色的使用者現在可以使用 DJ 指令。");
            }
        }
    }

}
