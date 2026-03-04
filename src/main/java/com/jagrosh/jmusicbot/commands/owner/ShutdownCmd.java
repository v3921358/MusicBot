package com.jagrosh.jmusicbot.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jmusicbot.Bot;
import com.jagrosh.jmusicbot.commands.OwnerCommand;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import net.dv8tion.jda.api.interactions.InteractionContextType;

/**
 *
 * @author John Grosh <john.a.grosh@gmail.com>
 */
public class ShutdownCmd extends OwnerCommand {
    private final Bot bot;

    public ShutdownCmd(Bot bot) {
        this.bot = bot;
        this.name = "shutdown";
        this.help = "安全地關閉機器人";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.contexts = new InteractionContextType[]{InteractionContextType.GUILD, InteractionContextType.BOT_DM};
    }

    @Override
    protected void execute(CommandEvent event) {
        event.reply(event.getClient().getWarning() + "正在關閉機器人...");
        bot.shutdown();
    }
}
