package ppssppftpbot.pccb.net.twitch;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

/*
 * A class mainly just for simplify some stuff so the code is cleaner. 
 * At least until my requested features are added lul.
 */
public class TwitchUtilities {

	/**
	 * Utility method to make sending twitch messages cleaner since the library currently has no quick reply method like JDA.
	 * @param event
	 * @param message
	 */
	public static void sendMessage(ChannelMessageEvent event, String message) {
		event.getTwitchChat().sendMessage(event.getChannel().getName(), message);
	}
}
