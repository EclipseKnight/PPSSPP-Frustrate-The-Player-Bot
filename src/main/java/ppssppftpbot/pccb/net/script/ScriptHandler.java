package ppssppftpbot.pccb.net.script;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ppssppftpbot.pccb.net.logger.Logger;
import ppssppftpbot.pccb.net.logger.Logger.Level;
import ppssppftpbot.pccb.net.twitch.TwitchBot;

public class ScriptHandler {
	
	static ExecutorService threadPoolBeforeTheHunt = Executors.newFixedThreadPool(5); 
	static ExecutorService threadPoolOnTheHunt = Executors.newSingleThreadExecutor();
	
	public static void executeOnTheHunt(String scriptName) {
		TwitchBot.loadConfiguration();
		String scriptPath = TwitchBot.configuration.getScripts().get(scriptName);
		if (scriptPath == null || scriptPath.isBlank()) {
			Logger.log(Level.ERROR, "Invalid Path: " + scriptPath);
			return;
		}
		
		Logger.log(Level.INFO, "Executing script " + scriptName);
		threadPoolOnTheHunt.execute(new ScriptExecuteTask(scriptPath));
		
	}
	
	public static void executeBeforeTheHunt(String scriptName) {
		String scriptPath = TwitchBot.configuration.getScripts().get(scriptName);
		if (scriptPath == null || scriptPath.isBlank()) {
			Logger.log(Level.ERROR, "Invalid Path: " + scriptPath);
			return;
		}
		
		Logger.log(Level.INFO, "Executing script " + scriptName);
		threadPoolBeforeTheHunt.execute(new ScriptExecuteTask(scriptPath));
	}
	
}

