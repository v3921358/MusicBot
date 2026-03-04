/*
 * Copyright 2018 John Grosh <john.a.grosh@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;

/**
 * 音樂指令抽象類別
 * 所有音樂相關指令（如播放、跳過、停止）都會繼承此類別，以確保統一的檢查邏輯。
 * * @author John Grosh <john.a.grosh@gmail.com>
 */
public abstract class MusicCommand extends Command {
    protected final Bot bot;
    protected boolean bePlaying;
    protected boolean beListening;

    public MusicCommand(Bot bot) {
        this.bot = bot;
        this.guildOnly = true; // 僅限伺服器（公會）使用
        this.category = new Category("音樂");
    }

    @Override
    protected void execute(CommandEvent event) {
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        TextChannel textChannel = settings.getTextChannel(event.getGuild());

        // 1. 檢查指令是否在指定的機器人文字頻道中使用
        if (textChannel != null && (event.getChannel() == null || event.getChannel().getIdLong() != textChannel.getIdLong())) {
            try {
                event.getMessage().delete().queue(); // 刪除非指定頻道的指令訊息
            } catch (PermissionException ignore) {
            }
            event.replyInDm(event.getClient().getError() + " 你只能在 " + textChannel.getAsMention() + " 使用該指令！");
            return;
        }

        bot.getPlayerManager().setUpHandler(event.getGuild()); // 初始化音訊處理器

        // 2. 檢查是否需要有音樂正在播放才能執行（例如 skip 指令）
        if (bePlaying && !((AudioHandler) event.getGuild().getAudioManager().getSendingHandler()).isMusicPlaying(event.getJDA())) {
            event.reply(event.getClient().getError() + " 必須要有音樂正在播放時才能使用此指令！");
            return;
        }

        // 3. 檢查使用者是否在語音頻道中，以及是否與機器人在同一個頻道
        if (beListening) {
            AudioChannel current = event.getGuild().getSelfMember().getVoiceState().getChannel();
            if (current == null)
                current = settings.getVoiceChannel(event.getGuild());

            GuildVoiceState userState = event.getMember().getVoiceState();

            // 檢查：使用者不在語音頻道、使用者靜音（無法聽歌）、或是機器人已在其他頻道
            if (!userState.inAudioChannel() || userState.isDeafened() || (current != null && !userState.getChannel().equals(current))) {
                event.reply(event.getClient().getError() + " 你必須在 " + (current == null ? "語音頻道" : current.getAsMention()) + " 內聆聽才能使用此指令！");
                return;
            }

            // 4. 檢查是否在 AFK（閒置）頻道
            VoiceChannel afkChannel = userState.getGuild().getAfkChannel();
            if (afkChannel != null && afkChannel.equals(userState.getChannel())) {
                event.reply(event.getClient().getError() + " 你不能在閒置 (AFK) 頻道使用該指令！");
                return;
            }

            // 5. 如果機器人還沒進入語音頻道，嘗試加入使用者的頻道
            if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
                try {
                    event.getGuild().getAudioManager().openAudioConnection(userState.getChannel());
                } catch (PermissionException ex) {
                    event.reply(event.getClient().getError() + " 我無法連接至 " + userState.getChannel().getAsMention() + "，請檢查我的權限！");
                    return;
                }
            }
        }

        // 通過所有檢查，執行實際的指令邏輯
        doCommand(event);
    }

    /**
     * 繼承此類別的子類別需實作此方法
     */
    public abstract void doCommand(CommandEvent event);
}