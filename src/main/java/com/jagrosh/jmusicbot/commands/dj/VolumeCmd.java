/*
 * 版權所有 2016 John Grosh <john.a.grosh@gmail.com>。
 *
 * 根據 Apache License, Version 2.0（以下簡稱「授權」）授權使用。
 * 除非遵守授權條款，否則不得使用此檔案。
 * 你可以在以下網址取得授權副本：
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * 除非法律要求或書面同意，否則軟體是「按原樣」提供，
 * 不附任何明示或暗示的保證。
 * 詳細請參閱授權條款。
 */
package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.OtherUtil;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class VolumeCmd extends DJCommand {
    public VolumeCmd(Bot bot) {
        super(bot);
        this.name = "volume";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.help = "設定或顯示音量";
        this.arguments = "[0-150]";
    }

    @Override
    public void doCommand(CommandEvent event) {
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        int volume = handler.getPlayer().getVolume();
        if (event.getArgs().isEmpty()) {
            event.reply(FormatUtil.volumeIcon(volume) + " 目前音量為 `" + volume + "`");
        } else {
            int nvolume;
            try {
                nvolume = Integer.parseInt(event.getArgs());
            } catch (NumberFormatException e) {
                nvolume = -1;
            }
            if (nvolume < 0 || nvolume > 150)
                event.reply(event.getClient().getError() + " 音量必須是介於 0 到 150 的有效整數！");
            else {
                handler.getPlayer().setVolume(nvolume);
                settings.setVolume(nvolume);
                event.reply(FormatUtil.volumeIcon(nvolume) + " 音量已從 `" + volume + "` 更改為 `" + nvolume + "`");
            }
        }
    }

}
