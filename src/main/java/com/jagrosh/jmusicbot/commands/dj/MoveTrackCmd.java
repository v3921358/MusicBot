package com.jagrosh.jmusicbot.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.audio.QueuedTrack;
import com.jagrosh.jmusicbot.commands.DJCommand;
import com.jagrosh.jmusicbot.queue.AbstractQueue;
import com.jagrosh.jmusicbot.utils.OtherUtil;

/**
 * 此指令允許使用者將播放清單中的歌曲移動到不同位置。
 */
public class MoveTrackCmd extends DJCommand {

    public MoveTrackCmd(Bot bot) {
        super(bot);
        this.name = "moveTrack";
        this.help = "將當前隊列中的歌曲移動到不同位置";
        this.arguments = "<從哪個位置> <到哪個位置>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) {
        int from;
        int to;

        String[] parts = event.getArgs().split("\\s+", 2);
        if (parts.length < 2) {
            event.reply(event.getClient().getError() + "請提供兩個有效的索引位置。");
            return;
        }

        try {
            // 驗證輸入的索引
            from = Integer.parseInt(parts[0]);
            to = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            event.reply(event.getClient().getError() + "請提供兩個有效的索引位置。");
            return;
        }

        if (from == to) {
            event.reply(event.getClient().getError() + "無法將歌曲移動到相同位置。");
            return;
        }

        // 驗證 from 與 to 是否在有效範圍內
        AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
        AbstractQueue<QueuedTrack> queue = handler.getQueue();
        if (isUnavailablePosition(queue, from)) {
            String reply = String.format("`%d` 不是隊列中的有效位置！", from);
            event.reply(event.getClient().getError() + reply);
            return;
        }
        if (isUnavailablePosition(queue, to)) {
            String reply = String.format("`%d` 不是隊列中的有效位置！", to);
            event.reply(event.getClient().getError() + reply);
            return;
        }

        // 執行歌曲移動
        QueuedTrack track = queue.moveItem(from - 1, to - 1);
        String trackTitle = track.getTrack().getInfo().title;
        String reply = String.format("已將 **%s** 從位置 `%d` 移動到 `%d`。", trackTitle, from, to);
        event.reply(event.getClient().getSuccess() + reply);
    }

    private static boolean isUnavailablePosition(AbstractQueue<QueuedTrack> queue, int position) {
        return (position < 1 || position > queue.size());
    }
}
