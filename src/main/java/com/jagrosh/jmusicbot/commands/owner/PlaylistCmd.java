package com.jagrosh.jmusicbot.commands.owner;

import java.io.IOException;
import java.util.List;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.playlist.PlaylistLoader.Playlist;
import com.jagrosh.jmusicbot.utils.OtherUtil;

public class PlaylistCmd extends OwnerCommand {
    private final Bot bot;

    public PlaylistCmd(Bot bot) {
        this.bot = bot;
        this.guildOnly = false;
        this.name = "playlist";
        this.arguments = "<append|delete|make|setdefault>";
        this.help = "播放清單管理";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.children = new OwnerCommand[]{
                new ListCmd(),
                new AppendlistCmd(),
                new DeletelistCmd(),
                new MakelistCmd(),
                new DefaultlistCmd(bot)
        };
    }

    @Override
    public void execute(CommandEvent event) {
        StringBuilder builder = new StringBuilder(event.getClient().getWarning() + " 播放清單管理指令:\n");
        for (Command cmd : this.children)
            builder.append("\n`").append(event.getClient().getPrefix()).append(name).append(" ").append(cmd.getName())
                    .append(" ").append(cmd.getArguments() == null ? "" : cmd.getArguments()).append("` - ").append(cmd.getHelp());
        event.reply(builder.toString());
    }

    public class MakelistCmd extends OwnerCommand {
        public MakelistCmd() {
            this.name = "make";
            this.aliases = new String[]{"create"};
            this.help = "建立新的播放清單";
            this.arguments = "<名稱>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String pname = event.getArgs().replaceAll("\\s+", "_");
            pname = pname.replaceAll("[*?|\\/\":<>]", "");
            if (pname == null || pname.isEmpty()) {
                event.reply(event.getClient().getError() + "請提供播放清單名稱!");
            } else if (bot.getPlaylistLoader().getPlaylist(pname) == null) {
                try {
                    bot.getPlaylistLoader().createPlaylist(pname);
                    event.reply(event.getClient().getSuccess() + " 成功建立播放清單 `" + pname + "`!");
                } catch (IOException e) {
                    event.reply(event.getClient().getError() + " 無法建立播放清單: " + e.getLocalizedMessage());
                }
            } else
                event.reply(event.getClient().getError() + " 播放清單 `" + pname + "` 已存在!");
        }
    }

    public class DeletelistCmd extends OwnerCommand {
        public DeletelistCmd() {
            this.name = "delete";
            this.aliases = new String[]{"remove"};
            this.help = "刪除現有播放清單";
            this.arguments = "<名稱>";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String pname = event.getArgs().replaceAll("\\s+", "_");
            if (bot.getPlaylistLoader().getPlaylist(pname) == null)
                event.reply(event.getClient().getError() + " 播放清單 `" + pname + "` 不存在!");
            else {
                try {
                    bot.getPlaylistLoader().deletePlaylist(pname);
                    event.reply(event.getClient().getSuccess() + " 成功刪除播放清單 `" + pname + "`!");
                } catch (IOException e) {
                    event.reply(event.getClient().getError() + " 無法刪除播放清單: " + e.getLocalizedMessage());
                }
            }
        }
    }

    public class AppendlistCmd extends OwnerCommand {
        public AppendlistCmd() {
            this.name = "append";
            this.aliases = new String[]{"add"};
            this.help = "向現有播放清單加入歌曲";
            this.arguments = "<名稱> <URL> | <URL> | ...";
            this.guildOnly = false;
        }

        @Override
        protected void execute(CommandEvent event) {
            String[] parts = event.getArgs().split("\\s+", 2);
            if (parts.length < 2) {
                event.reply(event.getClient().getError() + " 請提供播放清單名稱和欲加入的歌曲URL!");
                return;
            }
            String pname = parts[0];
            Playlist playlist = bot.getPlaylistLoader().getPlaylist(pname);
            if (playlist == null)
                event.reply(event.getClient().getError() + " 播放清單 `" + pname + "` 不存在!");
            else {
                StringBuilder builder = new StringBuilder();
                playlist.getItems().forEach(item -> builder.append("\r\n").append(item));
                String[] urls = parts[1].split("\\|");
                for (String url : urls) {
                    String u = url.trim();
                    if (u.startsWith("<") && u.endsWith(">"))
                        u = u.substring(1, u.length() - 1);
                    builder.append("\r\n").append(u);
                }
                try {
                    bot.getPlaylistLoader().writePlaylist(pname, builder.toString());
                    event.reply(event.getClient().getSuccess() + " 成功將 " + urls.length + " 首歌曲加入播放清單 `" + pname + "`!");
                } catch (IOException e) {
                    event.reply(event.getClient().getError() + " 無法將歌曲加入播放清單: " + e.getLocalizedMessage());
                }
            }
        }
    }

    public class DefaultlistCmd extends AutoPlayListCmd {
        public DefaultlistCmd(Bot bot) {
            super(bot);
            this.name = "setDefault";
            this.aliases = new String[]{"default"};
            this.arguments = "<播放清單名稱|NONE>";
            this.guildOnly = true;
        }
    }

    public class ListCmd extends OwnerCommand {
        public ListCmd() {
            this.name = "all";
            this.aliases = new String[]{"available", "list"};
            this.help = "列出所有可用播放清單";
            this.guildOnly = true;
        }

        @Override
        protected void execute(CommandEvent event) {
            if (!bot.getPlaylistLoader().folderExists())
                bot.getPlaylistLoader().createFolder();
            if (!bot.getPlaylistLoader().folderExists()) {
                event.reply(event.getClient().getWarning() + " 播放清單資料夾不存在且無法建立!");
                return;
            }
            List<String> list = bot.getPlaylistLoader().getPlaylistNames();
            if (list == null)
                event.reply(event.getClient().getError() + " 無法載入可用播放清單!");
            else if (list.isEmpty())
                event.reply(event.getClient().getWarning() + " 播放清單資料夾中沒有播放清單!");
            else {
                StringBuilder builder = new StringBuilder(event.getClient().getSuccess() + " 可用播放清單:\n");
                list.forEach(str -> builder.append("`").append(str).append("` "));
                event.reply(builder.toString());
            }
        }
    }
}
