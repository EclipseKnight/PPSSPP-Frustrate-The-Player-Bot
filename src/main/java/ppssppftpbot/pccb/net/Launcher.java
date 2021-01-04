package ppssppftpbot.pccb.net;


import org.fusesource.jansi.AnsiConsole;

import ppssppftpbot.pccb.net.twitch.TwitchBot;
import ppssppftpbot.pccb.net.websocket.WebSocketHandler;

public class Launcher {

	public static TwitchBot twitchBot;
	
	public static WebSocketHandler socketHandler;
	
	public static String configPath = System.getProperty("user.dir");
	
	public static void main(String[] args) {
		// allows ANSI escape sequences to format console output. For loggers.
		AnsiConsole.systemInstall();
		
		twitchBot = new TwitchBot();
		twitchBot.registerFeatures();
		twitchBot.start();
		
		socketHandler = new WebSocketHandler();
		
	}
}
