package ppssppftpbot.pccb.net;

import ppssppftpbot.pccb.net.twitch.TwitchBot;
import ppssppftpbot.pccb.net.websocket.WebSocketHandler;

public class Launcher {

	public static TwitchBot twitchBot;
	
	public static WebSocketHandler socketHandler;
	
	public static void main(String[] args) {
		twitchBot = new TwitchBot();
		twitchBot.registerFeatures();
		twitchBot.start();
		
		socketHandler = new WebSocketHandler();
	}
}
