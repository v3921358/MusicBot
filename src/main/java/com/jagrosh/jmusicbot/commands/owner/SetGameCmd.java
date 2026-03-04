/*
 * 版權所有 2017 John Grosh <john.a.grosh@gmail.com>。
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
package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.InteractionContextType;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class SetGameCmd extends OwnerCommand {
    public SetGameCmd(Bot bot) {
        this.name = "setGame";
        this.help = "設定機器人正在玩的遊戲";
        this.arguments = "[動作] [遊戲名稱]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM};
        this.children = new OwnerCommand[]{
                new SetlistenCmd(),
                new SetstreamCmd(),
                new SetwatchCmd()
        };
    }

    @Override
    protected void execute(CommandEvent event) {
        String title = event.getArgs().toLowerCase().startsWith("playing") ? event.getArgs().substring(7).trim() : event.getArgs();
        try {
            event.getJDA().getPresence().setActivity(title.isEmpty() ? null : Activity.playing(title));
            event.reply(event.getClient().getSuccess() + " **" + event.getSelfUser().getName()
                    + "** " + (title.isEmpty() ? "不再玩任何遊戲。" : "正在玩 `" + title + "`"));
        } catch (Exception e) {
            event.reply(event.getClient().getError() + " 無法設定遊戲！");
        }
    }

    private class SetstreamCmd extends OwnerCommand {
        private SetstreamCmd() {
            this.name = "stream";
            this.aliases = new String[]{"twitch", "streaming"};
            this.help = "將機器人的遊戲設為直播";
            this.arguments = "<Twitch使用者名稱> <遊戲名稱>";
            this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM};
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] parts = event.getArgs().split("\\s+", 2);
            if (parts.length < 2) {
                event.reply(event.getClient().getError() + "請提供 Twitch 使用者名稱以及要直播的遊戲名稱");
                return;
            }
            try {
                event.getJDA().getPresence().setActivity(Activity.streaming(parts[1], "https://twitch.tv/" + parts[0]));
                event.reply(event.getClient().getSuccess() + "**" + event.getSelfUser().getName()
                        + "** 現在正在直播 `" + parts[1] + "`");
            } catch (Exception e) {
                event.reply(event.getClient().getError() + " 無法設定遊戲！");
            }
        }
    }

    private class SetlistenCmd extends OwnerCommand {
        private SetlistenCmd() {
            this.name = "listen";
            this.aliases = new String[]{"listening"};
            this.help = "設定機器人正在聆聽的內容";
            this.arguments = "<標題>";
            this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM};
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getArgs().isEmpty()) {
                event.reply(event.getClient().getError() + "請提供要聆聽的標題！");
                return;
            }
            String title = event.getArgs().toLowerCase().startsWith("to") ? event.getArgs().substring(2).trim() : event.getArgs();
            try {
                event.getJDA().getPresence().setActivity(Activity.listening(title));
                event.reply(event.getClient().getSuccess() + "**" + event.getSelfUser().getName() + "** 現在正在聆聽 `" + title + "`");
            } catch (Exception e) {
                event.reply(event.getClient().getError() + " 無法設定遊戲！");
            }
        }
    }

    private class SetwatchCmd extends OwnerCommand {
        private SetwatchCmd() {
            this.name = "watch";
            this.aliases = new String[]{"watching"};
            this.help = "設定機器人正在觀看的內容";
            this.arguments = "<標題>";
            this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM};
        }

        @Override
        protected void execute(CommandEvent event) {
            if (event.getArgs().isEmpty()) {
                event.reply(event.getClient().getError() + "請提供要觀看的標題！");
                return;
            }
            String title = event.getArgs();
            try {
                event.getJDA().getPresence().setActivity(Activity.watching(title));
                event.reply(event.getClient().getSuccess() + "**" + event.getSelfUser().getName() + "** 現在正在觀看 `" + title + "`");
            } catch (Exception e) {
                event.reply(event.getClient().getError() + " 無法設定遊戲！");
            }
        }
    }
}
