package ppssppftpbot.pccb.net.twitch.features;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import ppssppftpbot.pccb.net.logger.Logger;
import ppssppftpbot.pccb.net.logger.Logger.Level;

public class WriteChannelChatToConsole {

    /**
     * Register events of this class with the EventManager/EventHandler
     *
     * @param eventHandler eventHandler
     */
    public WriteChannelChatToConsole(SimpleEventHandler eventHandler) {
        eventHandler.onEvent(ChannelMessageEvent.class, this::onChannelMessage);
    }

    /**
     * Subscribe to the ChannelMessage Event and write the output to the console
     */
    public void onChannelMessage(ChannelMessageEvent event) {
        Logger.log(Level.CHAT, String.format( "Channel [%s] - User[%s] - ID[%s] - Message [%s]",
                event.getChannel().getName(),
                event.getUser().getName(),
                event.getUser().getId(),
                event.getMessage()));
    }

}
