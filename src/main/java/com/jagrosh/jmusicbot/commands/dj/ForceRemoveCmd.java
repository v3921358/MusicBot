/*
 * Copyright 2019 John Grosh <john.a.grosh@gmail.com>.
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
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.utils.FinderUtil;
import com.jagrosh.jdautilities.menu.OrderedMenu;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Michaili K.
 */
public class ForceRemoveCmd extends DJCommand {
    public ForceRemoveCmd(Bot bot) {
        super(bot);
        this.name = "forceRemove";
        this.help = "從隊列中移除某使用者的所有歌曲";
        this.arguments = "<使用者>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.beListening = false;
        this.bePlaying = true;
        this.botPermissions = new Permission[]{Permission.MESSAGE_EMBED_LINKS};
    }

    @Override
    public void doCommand(CommandEvent event) {
        if (event.getArgs().isEmpty()) {
            event.reply(event.getClient().getError() + "你必須標註一位使用者！");
            return;
        }

        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        if (handler.getQueue().isEmpty()) {
            event.reply(event.getClient().getError() + "隊列中沒有任何歌曲！");
            return;
        }

        User target;
        List<Member> found = FinderUtil.findMembers(event.getArgs(), event.getGuild());

        if (found.isEmpty()) {
            event.reply(event.getClient().getError() + "找不到該使用者！");
            return;
        } else if (found.size() > 1) {
            // 如果找到多位使用者，顯示選單讓指令發起者選擇
            OrderedMenu.Builder builder = new OrderedMenu.Builder();
            for (int i = 0; i < found.size() && i < 4; i++) {
                Member member = found.get(i);
                builder.addChoice("**" + member.getUser().getName() + "**#" + member.getUser().getDiscriminator());
            }

            builder
                    .setSelection((msg, i) -> removeAllEntries(found.get(i - 1).getUser(), event))
                    .setText("找到多位使用者：")
                    .setColor(event.getSelfMember().getColor())
                    .useNumbers()
                    .setUsers(event.getAuthor())
                    .useCancelButton(true)
                    .setCancel((msg) -> {
                    })
                    .setEventWaiter(bot.getWaiter())
                    .setTimeout(1, TimeUnit.MINUTES)
                    .build().display(event.getChannel());

            return;
        } else {
            target = found.get(0).getUser();
        }

        removeAllEntries(target, event);

    }

    private void removeAllEntries(User target, CommandEvent event) {
        int count = ((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).getQueue().removeAll(target.getIdLong());
        if (count == 0) {
            event.reply(event.getClient().getWarning() + "**" + target.getName() + "** 在隊列中沒有任何歌曲！");
        } else {
            event.reply(event.getClient().getSuccess() + "成功從隊列中移除 `" + count + "` 首歌曲，來自 " + FormatUtil.formatUsername(target) + "。");
        }
    }
}
