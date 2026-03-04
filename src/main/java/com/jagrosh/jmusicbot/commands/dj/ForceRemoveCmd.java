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
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Michaili K.
 * Optimized by Gemini
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

        List<Member> found = FinderUtil.findMembers(event.getArgs(), event.getGuild());

        if (found.isEmpty()) {
            event.reply(event.getClient().getError() + "找不到該使用者！");
        } else if (found.size() > 1) {
            StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("forceRemove:menu")
                    .setPlaceholder("請選擇一位使用者")
                    .setRequiredRange(1, 1);

            for (int i = 0; i < found.size() && i < 25; i++) {
                Member member = found.get(i);
                menuBuilder.addOption(member.getEffectiveName() + " (" + member.getUser().getName() + ")", member.getUser().getId());
            }

            StringSelectMenu menu = menuBuilder.build();

            event.getChannel().sendMessage("找到多位使用者，請從下方選單選擇：")
                    .setComponents(ActionRow.of(menu))
                    .queue(msg -> {
                        // 使用 EventWaiter 監聽選擇事件
                        bot.getWaiter().waitForEvent(
                                StringSelectInteractionEvent.class,
                                e -> e.getMessageId().equals(msg.getId()) && e.getUser().equals(event.getAuthor()),
                                e -> {
                                    String userId = e.getInteraction().getValues().get(0);
                                    User target = event.getJDA().getUserById(userId);

                                    // 執行刪除邏輯
                                    int count = handler.getQueue().removeAll(Long.parseLong(userId));

                                    e.editMessage(renderReply(target, count, event))
                                            .setComponents() // 移除選單
                                            .queue();
                                },
                                1, TimeUnit.MINUTES,
                                () -> msg.editMessage("操作超時，已取消。").setComponents().queue()
                        );
                    });
        } else {
            // 只有一個使用者，直接移除
            User target = found.get(0).getUser();
            removeAllEntries(target, event);
        }
    }

    private void removeAllEntries(User target, CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        int count = handler.getQueue().removeAll(target.getIdLong());
        event.reply(renderReply(target, count, event));
    }

    private String renderReply(User target, int count, CommandEvent event) {
        if (count == 0) {
            return event.getClient().getWarning() + "**" + target.getName() + "** 在隊列中沒有任何歌曲！";
        } else {
            return event.getClient().getSuccess() + "成功從隊列中移除 `" + count + "` 首歌曲，來自 " + FormatUtil.formatUsername(target) + "。";
        }
    }
}