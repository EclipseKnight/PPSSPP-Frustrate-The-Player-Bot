package ppssppftpbot.pccb.net.twitch.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import ppssppftpbot.pccb.net.Launcher;
import ppssppftpbot.pccb.net.twitch.TwitchUtilities;

public class TwitchCommandWebSocketSend {

	public static void execute(ChannelMessageEvent event, String text) {
		TwitchUtilities.sendMessage(event, text);
		Launcher.socketHandler.send(text);
	}
}
