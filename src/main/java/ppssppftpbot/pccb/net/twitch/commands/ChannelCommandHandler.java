package ppssppftpbot.pccb.net.twitch.commands;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;

import ppssppftpbot.pccb.net.twitch.TwitchBot;

public class ChannelCommandHandler {

	public ChannelCommandHandler(SimpleEventHandler eventHandler) {
		eventHandler.onEvent(ChannelMessageEvent.class, event -> onChannelMessage(event));
	}

	public void onChannelMessage(ChannelMessageEvent event) {
		String msg = event.getMessage().toLowerCase();
		String prefix = TwitchBot.configuration.getPrefix();
		
		//Because Switch cases can't use non-constant values. I had to go with the less clean if (argument equals command name) approach. 		 
		
		// check if message is a command attempt. 
		if (msg.startsWith(prefix)) {

			// websocket commands
			if (msg.startsWith(prefix + "ws")) {
				String cmdWSSend = prefix + "ws " + TwitchBot.configuration.getFeatures().get("twitch_command_websocket_send").getName();
				// Get the text to send. 
				String arg = msg.substring(cmdWSSend.length());
				//execute command
				TwitchCommandWebSocketSend.execute(event, arg);
			}
			
			
			
			
			
			
			
			
			
			
		}
	}
}
