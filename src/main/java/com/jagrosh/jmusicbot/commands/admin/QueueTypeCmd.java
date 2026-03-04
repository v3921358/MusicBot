package com.jagrosh.jmusicbot.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.audio.AudioHandler;
import com.jagrosh.jmusicbot.commands.AdminCommand;
import com.jagrosh.jmusicbot.settings.QueueType;
import com.jagrosh.jmusicbot.settings.Settings;
import com.jagrosh.jmusicbot.utils.OtherUtil;

/**
 *
 * @author Wolfgang Schwendtbauer
 */
public class QueueTypeCmd extends AdminCommand {
    public QueueTypeCmd(Bot bot) {
        super();
        this.name = "queueType";
        this.help = "變更音樂隊列類型";
        this.arguments = "[" + String.join("|", QueueType.getNames()) + "]";
        this.aliases = bot.getConfig().getAliases(this.name);
    }

    @Override
    protected void execute(CommandEvent event) {
        String args = event.getArgs();
        QueueType value;
        Settings settings = event.getClient().getSettingsFor(event.getGuild());

        if (args.isEmpty()) {
            QueueType currentType = settings.getQueueType();
            event.reply(currentType.getEmoji() + " 目前的隊列類型為：`" + currentType.getUserFriendlyName() + "`。");
            return;
        }

        try {
            value = QueueType.valueOf(args.toUpperCase());
        } catch (IllegalArgumentException e) {
            event.reply(event.getClient().getError() + "無效的隊列類型。有效類型為：[" + String.join("|", QueueType.getNames()) + "]");
            return;
        }

        if (settings.getQueueType() != value) {
            settings.setQueueType(value);

            AudioHandler handler = (AudioHandler) event.getGuild().getAudioManager().getSendingHandler();
            if (handler != null)
                handler.setQueueType(value);
        }

        event.reply(value.getEmoji() + " 隊列類型已設定為 `" + value.getUserFriendlyName() + "`。");
    }
}
