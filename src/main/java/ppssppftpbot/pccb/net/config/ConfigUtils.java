package ppssppftpbot.pccb.net.config;

import ppssppftpbot.pccb.net.twitch.TwitchBot;

public class ConfigUtils {

	
	public static String getSnippet(String snippet) {
		return TwitchBot.configuration.getSnippets().get(snippet);
	}
	
	
}
