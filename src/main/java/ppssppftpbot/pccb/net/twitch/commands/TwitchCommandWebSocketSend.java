package ppssppftpbot.pccb.net.twitch.commands;

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import ppssppftpbot.pccb.net.Launcher;
import ppssppftpbot.pccb.net.logger.Logger;
import ppssppftpbot.pccb.net.logger.Logger.Level;
import ppssppftpbot.pccb.net.script.ScriptHandler;
import ppssppftpbot.pccb.net.twitch.TwitchBot;
import ppssppftpbot.pccb.net.twitch.TwitchUtilities;

public class TwitchCommandWebSocketSend {

	public static void execute(ChannelMessageEvent event, String text) {
		Logger.log(Level.INFO, "Command use detected");
		TwitchUtilities.sendMessage(event, text);
		ScriptHandler.executeOnTheHunt(text);
//		Launcher.socketHandler.send(text);
		Logger.log(Level.SUCCESS, "Command Sent!");
	}
} 
