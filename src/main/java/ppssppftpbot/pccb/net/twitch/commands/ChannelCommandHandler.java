package ppssppftpbot.pccb.net.twitch.commands;

import java.util.Arrays;

import com.github.philippheuer.events4j.simple.SimpleEventHandler;
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent;
import com.github.twitch4j.common.enums.CommandPermission;

import ppssppftpbot.pccb.net.logger.Logger;
import ppssppftpbot.pccb.net.logger.Logger.Level;
import ppssppftpbot.pccb.net.twitch.TwitchBot;

public class ChannelCommandHandler {

	public ChannelCommandHandler(SimpleEventHandler eventHandler) {
		eventHandler.onEvent(ChannelMessageEvent.class, this::onChannelMessage);
	}

	public void onChannelMessage(ChannelMessageEvent event) {
		String msg = event.getMessage();
		String prefix = TwitchBot.configuration.getPrefix();
		Logger.log(Level.INFO, msg);
		//Because Switch cases can't use non-constant values. I had to go with the less clean if (argument equals command name) approach. 		 
		
		// check if message is a command attempt. 
		if (msg.startsWith(prefix)) {

			// websocket commands
			String prefixWS = prefix + "ws ";
			
			// ws send command
			
			String cmdWSSend = prefixWS + TwitchBot.configuration.getFeature("twitch_command_websocket_send").getName();
			if (msg.startsWith(cmdWSSend) && isModOrBroadcaster(event)) {
				
				
				// Get the text to send. 
				String arg = msg.substring(cmdWSSend.length());
				
				// if there is no message following the command prompt e.g. !c ws send <nothing>.
				if (arg.isBlank()) {
					return;
				}
				
				String[] args = removeFirstArg(arg);
				
				// checks if a snippet is being used.
				if (getSnippet(args[0]) != null) {
					TwitchCommandWebSocketSend.execute(event, getSnippet(args[0]));
					return;
				}
				
				// if its not a snippet it will send the argument assuming its a manual attempt with no snippet. 
				TwitchCommandWebSocketSend.execute(event, arg.stripLeading());
			}
		}
	}
	
	
	
	/**
	 * method to check if it has to remove the first argument of the message due to weird split issue. Read comments in method. 
	 * @param arg
	 * @return
	 */
	private String[] removeFirstArg(String arg) {
		// get an array of arguments in case there is a snippet. 
		String[] args = arg.split("\\s+");
		
		// For whatever reason splitting white spaces will set arg[0] to nothing if there is a space after the send command
		// e.g. "!c ws send test" will yield an array of [, test]. This just removes that first argument if it happens.
		if (args.length > 1) {
			if (args[0].isBlank())
				return args = Arrays.copyOfRange(args, 1, args.length);
		}
		
		return args;
	}
	
	/**
	 * Method is used to get the snippet 
	 * @return
	 */
	private String getSnippet(String snippet) {
		return TwitchBot.configuration.getSnippets().get(snippet);
	}
	
	/**
	 * method used to check if user is a broadcaster or a moderator
	 * @param event
	 * @return
	 */
	private boolean isModOrBroadcaster(ChannelMessageEvent event) {
		return (event.getPermissions().contains(CommandPermission.BROADCASTER) || event.getPermissions().contains(CommandPermission.MODERATOR));
	}
}
