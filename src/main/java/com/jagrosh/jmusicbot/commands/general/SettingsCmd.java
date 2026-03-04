/*
 * Copyright 2017 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * 您不可在未遵守此授權條款的情況下使用本檔案。
 * 您可以在以下網址取得授權內容：
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * 除非法律要求或書面同意，
 * 否則依照授權條款分發的軟體皆以「原樣」提供，
 * 不附帶任何明示或默示的擔保或條件。
 * 詳細內容請參閱授權條款。
 */
package com.jagrosh.jmusicbot.commands.general;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.settings.QueueType;
import com.jagrosh.jmusicbot.settings.RepeatMode;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SettingsCmd extends Command {
    private final static String EMOJI = "\uD83C\uDFA7"; // 🎧

    public SettingsCmd(Bot bot) {
        this.name = "settings";
        this.help = "顯示機器人的設定";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD};
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        MessageCreateBuilder builder = new MessageCreateBuilder()
                .setContent(EMOJI + " **" + FormatUtil.filter(event.getSelfUser().getName()) + "** 的設定：");
        TextChannel tchan = s.getTextChannel(event.getGuild());
        VoiceChannel vchan = s.getVoiceChannel(event.getGuild());
        Role role = s.getRole(event.getGuild());
        EmbedBuilder ebuilder = new EmbedBuilder()
                .setColor(event.getSelfMember().getColors().getPrimary())
                .setDescription("文字頻道： " + (tchan == null ? "不限" : "**#" + tchan.getName() + "**")
                        + "\n語音頻道： " + (vchan == null ? "不限" : vchan.getAsMention())
                        + "\nDJ 身分組： " + (role == null ? "無" : "**" + role.getName() + "**")
                        + "\n自訂前綴： " + (s.getPrefix() == null ? "無" : "`" + s.getPrefix() + "`")
                        + "\n重複模式： " + (s.getRepeatMode() == RepeatMode.OFF
                        ? s.getRepeatMode().getUserFriendlyName()
                        : "**" + s.getRepeatMode().getUserFriendlyName() + "**")
                        + "\n隊列模式： " + (s.getQueueType() == QueueType.FAIR
                        ? s.getQueueType().getUserFriendlyName()
                        : "**" + s.getQueueType().getUserFriendlyName() + "**")
                        + "\n預設播放清單： " + (s.getDefaultPlaylist() == null ? "無" : "**" + s.getDefaultPlaylist() + "**")
                )
                .setFooter(event.getJDA().getGuilds().size() + " 個伺服器 | "
                        + event.getJDA().getGuilds().stream().filter(g -> g.getSelfMember().getVoiceState().inAudioChannel()).count()
                        + " 個語音連線", null);
        event.getChannel().sendMessage(builder.setEmbeds(ebuilder.build()).build()).queue();
    }

}
