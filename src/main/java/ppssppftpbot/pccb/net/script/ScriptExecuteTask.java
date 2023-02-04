package ppssppftpbot.pccb.net.script;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import ppssppftpbot.pccb.net.Launcher;
import ppssppftpbot.pccb.net.logger.Logger;
import ppssppftpbot.pccb.net.logger.Logger.Level;
import ppssppftpbot.pccb.net.script.keywords.Keywords.Keyword;

public class ScriptExecuteTask implements Runnable {

	private String scriptPath;
	private Map<String, String> variableMap;
	
	//key: if# value: lineNum
	private Map<String, Boolean> ifMap;
	
	private final Pattern pattern = Pattern.compile("!|!=|<=|>=|\\|\\||\\&\\&|\\d+|[a-z()+\\-*/<>]");
	private final Matcher matcher = pattern.matcher("");
	
	
	public ScriptExecuteTask(String scriptPath) {
		this.scriptPath = scriptPath;
		variableMap = new HashMap<String, String>();
		ifMap = new HashMap<String, Boolean>();
		
	}
	
	@Override
	public void run() {
		try(LineNumberReader br = new LineNumberReader(new FileReader(scriptPath))) {
		    String line;

		    while ((line = br.readLine()) != null) {
		    	//Skip blank lines in script
		    	if (line.isBlank()) {
		    		continue;
		    	}
		    	
		    	//Ignore comments
		    	if (line.stripLeading().startsWith("#")) {
		    		continue;
		    	}
		    	
		    	Logger.log(Level.INFO, br.getLineNumber() + "| " + line);
		    	parseLine(line, br.getLineNumber());
		    }
		    
		    
		} catch (FileNotFoundException e) {
			Logger.log(Level.ERROR, "FileNotFoundException: " + e.getStackTrace());
		} catch (IOException e) {
			Logger.log(Level.ERROR, "IOException: " + e.getStackTrace());
		}
		
		variableMap.clear();
		
		Logger.log(Level.SUCCESS, "Script Execution Finished!");
	}
	
	private boolean parseLine(String line, int lineNum) {
		line = line.stripLeading();
		//args[0] = the keyword
		//args[n] = whatever args for that keyword 
		String[] args = line.split("\\s+");
		Keyword kw = Keyword.valueOf(args[0].toUpperCase());
		args = Arrays.copyOfRange(args, 1, args.length);
		
		//(if the "if" conditional is false, continue until paired END where it will remove it from the map.
		//Checks previous found if condition, if its false then set next if to false, and remove last if when end
    	if (ifMap.get("if" + (ifMap.size()-1)) != null && Boolean.FALSE.equals(ifMap.get("if" + (ifMap.size()-1)))) {
    		
    		if (kw.equals(Keyword.IF)) {
    			ifMap.put("if" + ifMap.size(), false);
    		}
    		
    		
    		if (kw.equals(Keyword.END)) {
    			ifMap.remove("if" + (ifMap.size()-1));
    		}
    		
    		return false;
    	}
		
		//check and handle the corresponding keyword
		switch (kw) {
			case WRITE -> handleWrite(args, lineNum);
			case READ -> handleRead(args, lineNum);
			case WAIT -> handleWait(args, lineNum);
			case BREAKPOINT -> handleBreakpoint(args, lineNum);
			case VAR -> handleVar(args, lineNum);
			case IF -> handleIf(args, lineNum);
			case END -> handleEnd(args, lineNum);
			case PRINT -> handlePrint(args, lineNum);
			case ADD -> handleAdd(args, lineNum);
			default -> Logger.log(Level.ERROR, "Invalid Keyword: line " + lineNum + " of script located at \n" + scriptPath);
		}
		
		return false;
	}

	//syntax prefixes
	private static final String snipPrefix = "&";
	private static final String varPrefix = "$";
	
	//args: byte, address, value
	private void handleWrite(String[] args, int lineNum) {
		//check if using a snippet
		if (args[0].startsWith(snipPrefix)) {
			String snippet = args[0].substring(1);
			Launcher.socketHandler.sendAndWait(new JSONObject(snippet));
			return;
		}
		
		//Check to make sure appropriate byte size was passed
		if (!("u8".equalsIgnoreCase(args[0]) || !"u16".equalsIgnoreCase(args[0]) || !"u32".equalsIgnoreCase(args[0]))) {
			Logger.log(Level.ERROR, "Invalid Argument[line " + lineNum + "]: " + args[0] + " must be either u8, u16, or u32");
			return;
		}
		
		//manual usage.
		if (args.length != 3) {
			Logger.log(Level.ERROR, "Invalid Number Of Arguments[line " + lineNum + "]:  of script located at \n" + scriptPath);
			return;
		}
		
		//substitute var for their values.
		args = subVars(args, lineNum);
		if (args.length <= 0) {
					return;
		}
		
		String msg = String.format("{\"event\":\"memory.write_%s\",\"address\":%s,\"value\":%s}", args[0], args[1], args[2]);
		
		Launcher.socketHandler.sendAndWait(new JSONObject(msg));
		
	}
	
	//READ bytes u8,u16,u32
	//READ address var
	//READ snippet var
	private void handleRead(String[] args, int lineNum) {
		if (args.length > 3 || args.length < 1) {
			Logger.log(Level.ERROR, "Invalid Number Of Arguments[line " + lineNum + "]: of script located at \n" + scriptPath);
			return;
		}
		
		//Check to make sure appropriate byte size was passed
		if (!("u8".equalsIgnoreCase(args[0]) || !"u16".equalsIgnoreCase(args[0]) || !"u32".equalsIgnoreCase(args[0]))) {
			Logger.log(Level.ERROR, "Invalid Argument[line " + lineNum + "]: " + args[0] + " must be either u8, u16, or u32");
			return;
		}
		
		boolean isSnippet = args[1].startsWith(snipPrefix);
		boolean usingValidVar = args[1].startsWith(varPrefix)
				&& variableMap.containsKey(args[1].substring(varPrefix.length()));
		
		
		//Check and get var to assign result to.
		String var = null;
		if (args.length == 3 && args[2].startsWith(varPrefix)) {
			var = args[2].substring(varPrefix.length());
			if (!variableMap.containsKey(var)) {
				Logger.log(Level.ERROR, "Invalid Variable[line " + lineNum + "]: " + var + " was not declared.\n" + scriptPath);
				return;
			}
		}
		
		String result = null;
		
		//check if using a snippet
		//SNIPPET Usage
		if (isSnippet) {
			String snippet = args[1].substring(1);
			
			if (var != null) {
				result = parseResult(Launcher.socketHandler.sendAndWait(new JSONObject(snippet)));
				
				if ("error".equals(result)) {
					return;
				}
				variableMap.put(var, result);
			
			}
			return;
		}
				
		
		//MANUAL Usage (no SNIPPET)
		//check if using a var for the address	
		if (usingValidVar) {
			args[1] = variableMap.get(args[1].substring(varPrefix.length()));
			if (args[0] == null) {
				Logger.log(Level.ERROR, "Null Exception[line + " + lineNum + "]: address parameter is null.\n" + scriptPath);
				return;
			}
		}
		
		
		JSONObject msg = new JSONObject();
		msg.put("event", "memory.read_" + args[0]);
		msg.put("address", args[1]);
				
		//Send the message and wait for a response.
		result = parseResult(Launcher.socketHandler.sendAndWait(msg));
		
		if ("error".equals(result)) {
			return;
		}
		variableMap.put(var, result);
	}
	
	
	//DELAY 
	private void handleWait(String[] args, int lineNum) {
		
		if (args.length < 2) {
			Logger.log(Level.ERROR, "Invalid Number Of Arguments: line " + lineNum + " of script located at \n" + scriptPath);
			return;
		}
		
		TimeUnit time = switch (args[1].toUpperCase()) {
			case "MS", "MILLISECONDS": 
				yield TimeUnit.MILLISECONDS;
			
			case "SEC", "S", "SECONDS":
				yield TimeUnit.SECONDS;
			
			case "MIN", "MINUTES": 
				yield TimeUnit.MINUTES;
			
			case "H", "HR", "HOURS":
				yield TimeUnit.HOURS;
			
			default: {
				Logger.log(Level.ERROR, "Invalid Time Unit[line " + lineNum + "]: " + args[1] + ". Defaulted to seconds.");
				yield TimeUnit.SECONDS;
			}
			
		};
		
		try {
			//Block thread for given time
			long duration = TimeUnit.MILLISECONDS.convert(Long.parseLong(args[0]), time);
			Logger.log(Level.INFO, "Waiting " + args[0] + " " + time.name());
			
			//get monitor lock
			synchronized (this) {
				this.wait(duration);
			}
			
		} catch (NumberFormatException | InterruptedException e) {
			Logger.log(Level.ERROR, "Invalid Time Duration[line " + lineNum +"]: " + args[0] + ".");
		}
		
		
	}
	
	//(add, update, remove) address, size, enabled, log, read, write, change, logFormat, scriptToRun
	private void handleBreakpoint(String[] args, int lineNum) {
		if (!(args.length == 1 || args.length == 10 || args.length == 2)) {
			Logger.log(Level.ERROR, "Invalid Number Of Arguments: line " + lineNum + " of script located at \n" + scriptPath);
			return;
		}
		
		//substitute var for their values.
		args = subVars(args, lineNum);
		if (args.length <= 0) {
			return;
		}
		
		//resume from stepping
		if ("resume".equalsIgnoreCase(args[0])) {
			JSONObject msg = new JSONObject()
					.put("event", "cpu.resume");
			Launcher.socketHandler.send(msg);
			return;
		}

		//add a breakpoint
		if ("add".equalsIgnoreCase(args[0]) && args.length == 10) {
			JSONObject msg = new JSONObject()
					.put("event", "memory.breakpoint." + args[0].toLowerCase())
					.put("address", JSONObject.stringToValue(args[1]))
					.put("size", JSONObject.stringToValue(args[2]))
					.put("enabled", JSONObject.stringToValue(args[3]))
					.put("log", JSONObject.stringToValue(args[4]))
					.put("read", JSONObject.stringToValue(args[5]))
					.put("write", JSONObject.stringToValue(args[6]))
					.put("change", JSONObject.stringToValue(args[7]))
					.put("logFormat", args[8])
					.put("scriptToRun", args[9]);
			
			Launcher.socketHandler.sendAndWait(msg);
			return;
		}
		
		//remove a breakpoint
		if ("remove".equalsIgnoreCase(args[0]) && args.length == 2) {
			JSONObject msg = new JSONObject()
					.put("event", "memory.breakpoint." + args[0].toLowerCase())
					.put("logFormat", args[1]);
			
			Launcher.socketHandler.sendAndWait(msg);
		}
		
		
	}
	
	//varname, value
	private void handleVar(String[] args, int lineNum) {
		if (args.length < 1) {
			Logger.log(Level.ERROR, "Invalid Number Of Arguments: line " + lineNum + " of script located at \n" + scriptPath);
			return; 
		}
		
		if (args.length == 2) {
			variableMap.put(args[0], args[1]);
		}
		
		if (args.length == 1) {
			variableMap.put(args[0], null);
		}
		
		
	}
	
	private void handleIf(String[] args, int lineNum) {
		List<String> chars = new ArrayList<>();
		
		matcher.reset(String.join("", args));
		
		while (matcher.find()) {
			chars.add(matcher.group());
		}
		
		boolean result = false;
		for (int i = 0; i < chars.size(); i++) {
			if ("(".equals(chars.get(i))) {
				
				String left = chars.get(i+1);
				String right = chars.get(i+3);
				
				left = variableMap.get(left) != null ? variableMap.get(left) : left;
				right = variableMap.get(right) != null ? variableMap.get(right) : right;
				
				
				result = switch (chars.get(i+2)) {
					case "==": yield conditionIsEqual(left, right);
					case "!=": yield conditionNotEqual(left, right);
					case ">": yield conditionGreater(left, right);
					case "<": yield conditionLess(left, right);
					case ">=": yield conditionGreaterOrEqual(left, right);
					case "<=": yield conditionLessOrEqual(left, right);
					
					default: {
						Logger.log(Level.ERROR,"Invalid Operator: " + chars.get(i+2));	
						yield false;
					}
				};
			}
		}
		
		if ("!".equals(chars.get(0))) {
			ifMap.put("if" + ifMap.size(), !result);
			return;
		}
		
		ifMap.put("if" + ifMap.size(), result);
	}

	private void handleEnd(String[] args, int lineNum) {
		ifMap.remove("if" + (ifMap.size()-1));
	}
	
	private void handlePrint(String[] args, int lineNum) {
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith(varPrefix) && variableMap.containsKey(args[i].replace(varPrefix, "")) ) {
				args[i] = variableMap.get(args[i].replace(varPrefix, ""));
			}
		}
		
		Logger.log(Level.OUTPUT, lineNum + "| " + String.join(" ", args));
	}
	
	private void handleAdd(String[] args, int lineNum) {
		if (args.length < 3) {
			Logger.log(Level.ERROR, "Invalid Number Of Arguments: line " + lineNum + " of script located at \n" + scriptPath);
			return;
		}
		String left = args[0].replace(varPrefix, "");
		String right = args[1].replace(varPrefix, "");
		String var = args[2].replace(varPrefix, "");
		
		if (variableMap.get(left) != null) {
			left = variableMap.get(left);
		} 
		
		if (variableMap.get(right) != null) {
			right = variableMap.get(right);
		}
		
		mathAdd(left, right, var);
	}
	
	
	/**
	 * 
	 * @param result - result string from response.
	 * @return - value at address.
	 */
	private String parseResult(String result) {
		JSONObject msg = new JSONObject(result);
		if ("error".equals(msg.getString("event"))) {
			Logger.log(Level.ERROR, "Event Error: " + msg.getString("message")+ ", level: " + msg.getString("level"));
			return "error";
		}
		
		return String.valueOf(msg.getInt("value"));
	
	}
	
	/**
	 * 
	 * @param left
	 * @param right
	 * @return true if left is equal to right.
	 */
	private boolean conditionIsEqual(String left, String right) {
		try {
			return Integer.parseInt(left) == Integer.parseInt(right);
		} catch (NumberFormatException e) {
			Logger.log(Level.ERROR, "NumberFormatException: " + e.getMessage());
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param left
	 * @param right
	 * @return true if left is not equal to right.
	 */
	private boolean conditionNotEqual(String left, String right) {
		try {
			return Integer.parseInt(left) != Integer.parseInt(right);
		} catch (NumberFormatException e) {
			Logger.log(Level.ERROR, "NumberFormatException: " + e.getMessage());
		}
		
		return false;
	}
	
	
	/**
	 * 
	 * @param left
	 * @param right
	 * @return true if left is less than right.
	 */
	private boolean conditionLess(String left, String right) {
		try {
			return Integer.parseInt(left) < Integer.parseInt(right);
		} catch (NumberFormatException e) {
			Logger.log(Level.ERROR, "NumberFormatException: " + e.getMessage());
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param left
	 * @param right
	 * @return true if left is less than or equal to right.
	 */
	private boolean conditionLessOrEqual(String left, String right) {
		try {
			return Integer.parseInt(left) <= Integer.parseInt(right);
		} catch (NumberFormatException e) {
			Logger.log(Level.ERROR, "NumberFormatException: " + e.getMessage());
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param left
	 * @param right
	 * @return true if left is greater than right.
	 */
	private boolean conditionGreater(String left, String right) {
		
		
		try {
			return Integer.parseInt(left) > Integer.parseInt(right);
		} catch (NumberFormatException e) {
			Logger.log(Level.ERROR, "NumberFormatException: " + e.getMessage());
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param left
	 * @param right
	 * @return true if left is greater than or equal to right.
	 */
	private boolean conditionGreaterOrEqual(String left, String right) {
		try {
			return Integer.parseInt(left) >= Integer.parseInt(right);
		} catch (NumberFormatException e) {
			Logger.log(Level.ERROR, "NumberFormatException: " + e.getMessage());
		}
		
		return false;
	}
	
	
	/**
	 * 
	 * @param left int
	 * @param right int
	 * @param var varName
	 */
	private void mathAdd(String left, String right, String var) {
		
		if (!variableMap.containsKey(var.replace(varPrefix, ""))) {
			Logger.log(Level.ERROR, "Invalid Argument: " + var + " is not declared.");
			return;
		}
		
		try {
			variableMap.put(var.replace(varPrefix, ""), (Integer.parseInt(left) + Integer.parseInt(right)) + "");
		} catch (NumberFormatException e) {
			Logger.log(Level.ERROR, "NumberFormatException: " + e.getMessage());
		}
		
	}
	
	
	/**
	 * Substitutes variables used as arguments for their stored values.
	 * @param args
	 * @param lineNum
	 * @return an empty array if there was an invalid variable or the array with substituted values
	 */
	private String[] subVars(String[] args, int lineNum) {
		
		for (int i = 0; i < args.length; i++) {
			//Check if arg starts with variable prefix noting it is a variable being used.
			if (!args[i].startsWith(varPrefix)) {
				continue;
			}
			String varName = args[i].replace("$", "");

			//get the variable from the map, if it equals null then it does not exist or was not declared.
			String var = variableMap.get(varName);
			if (var == null) {
				Logger.log(Level.ERROR, "Invalid Variable[line " + lineNum + "]: " + varName + " was not declared.\n" + scriptPath);
				return new String[] {};
			}
			
			args[i] = var;
		}
		
		return args;
	}
}
