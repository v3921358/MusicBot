package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.utils.FileUpload;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class DebugCmd extends OwnerCommand {
    private final static String[] PROPERTIES = {"java.version", "java.vm.name", "java.vm.specification.version",
            "java.runtime.name", "java.runtime.version", "java.specification.version", "os.arch", "os.name"};

    private final Bot bot;

    public DebugCmd(Bot bot) {
        this.bot = bot;
        this.name = "debug";
        this.help = "顯示除錯資訊";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM};
    }

    @Override
    protected void execute(CommandEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("```\n系統屬性:");
        for (String key : PROPERTIES)
            sb.append("\n  ").append(key).append(" = ").append(System.getProperty(key));
        sb.append("\n\nJMusicBot 資訊:")
                .append("\n  版本 = ").append(OtherUtil.getCurrentVersion())
                .append("\n  擁有者 = ").append(bot.getConfig().getOwnerId())
                .append("\n  前綴 = ").append(bot.getConfig().getPrefix())
                .append("\n  替代前綴 = ").append(bot.getConfig().getAltPrefix())
                .append("\n  最大秒數 = ").append(bot.getConfig().getMaxSeconds())
                .append("\n  使用播放中圖片 = ").append(bot.getConfig().useNPImages())
                .append("\n  歌曲狀態顯示 = ").append(bot.getConfig().getSongInStatus())
                .append("\n  保留在語音頻道 = ").append(bot.getConfig().getStay())
                .append("\n  使用 Eval = ").append(bot.getConfig().useEval())
                .append("\n  更新提醒 = ").append(bot.getConfig().useUpdateAlerts());
        sb.append("\n\n依賴程式庫資訊:")
                .append("\n  JDA 版本 = ").append(JDAInfo.VERSION)
                .append("\n  JDA-Utilities 版本 = ").append(JDAUtilitiesInfo.VERSION)
                .append("\n  Lavaplayer 版本 = ").append(PlayerLibrary.VERSION);
        long total = Runtime.getRuntime().totalMemory() / 1024 / 1024;
        long used = total - (Runtime.getRuntime().freeMemory() / 1024 / 1024);
        sb.append("\n\n執行時資訊:")
                .append("\n  總記憶體 = ").append(total)
                .append("\n  已使用記憶體 = ").append(used);
        sb.append("\n\nDiscord 資訊:")
                .append("\n  ID = ").append(event.getJDA().getSelfUser().getId())
                .append("\n  伺服器數量 = ").append(event.getJDA().getGuildCache().size())
                .append("\n  使用者數量 = ").append(event.getJDA().getUserCache().size());
        sb.append("\n```");

        if (event.isFromType(ChannelType.PRIVATE)
                || event.getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_ATTACH_FILES))
            event.getChannel().sendFiles(FileUpload.fromData(sb.toString().getBytes(), "debug_information.txt")).queue();
        else
            event.reply("除錯資訊: " + sb.toString());
    }
}
