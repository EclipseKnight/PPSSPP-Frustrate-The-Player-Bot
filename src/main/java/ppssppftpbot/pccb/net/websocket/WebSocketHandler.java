package ppssppftpbot.pccb.net.websocket;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import ppssppftpbot.pccb.net.Launcher;
import ppssppftpbot.pccb.net.logger.Logger;
import ppssppftpbot.pccb.net.logger.Logger.Level;
import ppssppftpbot.pccb.net.script.ScriptHandler;
import ppssppftpbot.pccb.net.script.requests.Breakpoint;
import ppssppftpbot.pccb.net.script.requests.Request;

public class WebSocketHandler {

	
	public static WebSocketConfiguration configuration;
	
	public static WebSocketClient ppssppClient;

	/**
	 * Used to prevent reconnect spam. Only allow 1 reconnect attempt at a time. 
	 */
	public static boolean attemptingReconnect = false;
	
	public static URI serverUri;
	
	public long currentTicketNum = 0;
		
	public ArrayList<Request> requests = new ArrayList<>();
	
	public Map<String, Breakpoint> breakpoints = new HashMap<>();
	
	public WebSocketHandler() {
		// Load Configuration
		loadConfiguration();
		Logger.log(Level.INFO, configuration.toString());
		// Creates a new WebSocketClient from the PPSSPPClient
		serverUri = configuration.getServerUri();
		
		// Check if address is to obtained automatically or not.
		if (serverUri.toString().contains(":auto")) {
			autoConnect();
		} else {
			ppssppClient = new PPSSPPClient(serverUri);
			connect();
		}

	}
	
	
	/**
	 * Initializes connection with WebSocket Server
	 */
	public void connect() {
		// Connects to the WebSocket server
		if (ppssppClient != null)
			ppssppClient.connect();
	}
	
	/**
	 * Initiates the websocket close handshake
	 */
	public void close() {
		if (ppssppClient != null)
			ppssppClient.close();
	}
	
	
	public void send(String text) {
		if (isClosed()) {
			return;
		}
		
		ppssppClient.send(text);
		System.out.println(text);
		Logger.log(Level.INFO, text);
	}
	
	/**
	 * Sends a text to the connected websocket server
	 * @param message the string which will be transmitted
	 */
	public void send(JSONObject message, CompletableFuture<String> responseFuture) {
		if (isClosed()) {
			return;
		}
		
		//attach ticket number to message to ensure proper pairing of response.
		message.put("ticket", nextTicketNum());
		
		Request req = new Request(message.getString("event"), message.toString(), responseFuture, message.getLong("ticket"));
		
		requests.add(0, req);
		ppssppClient.send(message.toString());
		Logger.log(Level.INFO, message.toString());
	}
	
	
	/**
	 * Sends message and waits for the response blocking the thread until then.
	 * @param msg
	 * @return resulting string or null
	 */
	public String sendAndWait(JSONObject msg) {
		if (isClosed()) {
			return null;
		}
		
		//attach ticket number to message to ensure proper pairing of response.
		msg.put("ticket", nextTicketNum());
		
		//Check if breakpoint and if so build breakpoint request.
		String event = msg.getString("event");
		if (event.contains("memory.breakpoint")) {
			String bpName = msg.getString("logFormat");
			
			//if adding a new breakpoint
			if (event.contains("add") || event.contains("update")) {
				//create breakpoint object
				Breakpoint bp = new Breakpoint(msg.getInt("address"),
						msg.getInt("size"),
						msg.getBoolean("enabled"),
						msg.getBoolean("read"),
						msg.getBoolean("write"),
						msg.getBoolean("change"),
						bpName,
						msg.getString("scriptToRun"),
						msg.getLong("ticket"));
				msg.remove("scriptToRun");
				
				//check if it already exists in map
				if (event.contains("add") && breakpoints.containsKey(bpName)) {
					Logger.log(Level.ERROR, "Breakpoint with the name \"" + bpName + "\" already exists. Breakpoint ignored.");
					return null;
				}
				
				//add breakpoint
				breakpoints.put(bpName, bp);
				requests.add(0, bp);
				ppssppClient.send(msg.toString());
				Logger.log(Level.INFO, msg.toString());
				return waitForFutureResponse(bp.getResponseFuture());
			}
			
			//if removing a breakpoint
			if (event.contains("remove")) {
				//fetch from map if it exists
				Breakpoint bp = breakpoints.get(bpName);
				if (bp == null) {
					Logger.log(Level.ERROR, "Breakpoint with the name \"" + bpName + "\" does not exist. Breakpoint ignored.");
					return null;
				}
				
				//form json request.
				JSONObject msg2 = new JSONObject()
						.put("event", event)
						.put("address", bp.getAddress())
						.put("size", bp.getSize())
						.put("ticket", msg.getLong("ticket"));
				
				breakpoints.remove(bpName);
				requests.add(0, bp);
				ppssppClient.send(msg2.toString());
				Logger.log(Level.INFO, msg2.toString());
				return waitForFutureResponse(bp.getResponseFuture());
			}
		}	
		
		
		
		Request req = new Request(msg.getString("event"), msg.toString(), msg.getLong("ticket"));
		
		requests.add(0, req);
		ppssppClient.send(msg.toString());
		Logger.log(Level.INFO, msg.toString());
		
		return waitForFutureResponse(req.getResponseFuture());
	}
	
	/**
	 * Pairs with send(String, CompletableFuture) method to get result.
	 * @param responseFuture
	 * @return
	 */
	public String waitForFutureResponse(CompletableFuture<String> responseFuture) {
		
		try {

			return responseFuture.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void send(JSONObject msg) {
		if (isClosed()) {
			return;
		}

		
		String event = msg.getString("event");
		if (event.contains("memory.breakpoint")) {
			String bpName = msg.getString("logFormat");
			
			//if adding a new breakpoint
			if (event.contains("add") || event.contains("update")) {
				//create breakpoint object
				Breakpoint bp = new Breakpoint(msg.getInt("address"),
						msg.getInt("size"),
						msg.getBoolean("enabled"),
						msg.getBoolean("read"),
						msg.getBoolean("write"),
						msg.getBoolean("change"),
						bpName,
						msg.getString("scriptToRun"),
						nextTicketNum());
				msg.remove("scriptToRun");
				
				//check if it already exists in map
				if (event.contains("update") && breakpoints.containsKey(bpName)) {
					Logger.log(Level.ERROR, "Breakpoint with the name \"" + bpName + "\" already exists. Breakpoint ignored.");
					return;
				}
				
				//add breakpoint
				breakpoints.put(bpName, bp);
				ppssppClient.send(msg.toString());
				Logger.log(Level.INFO, msg.toString());
				return;
			}
			
			//if removing a breakpoint
			if (event.contains("remove")) {
				//fetch from map if it exists
				Breakpoint bp = breakpoints.get(bpName);
				if (bp == null) {
					Logger.log(Level.ERROR, "Breakpoint with the name \"" + bpName + "\" does not exist. Breakpoint ignored.");
					return;
				}
				
				//form json request.
				JSONObject msg2 = new JSONObject()
						.put("event", event)
						.put("address", bp.getAddress())
						.put("size", bp.getSize());
				
				breakpoints.remove(bpName);
				ppssppClient.send(msg2.toString());
				Logger.log(Level.INFO, msg2.toString());
				return;
			}
		}
		
		
		if (event.contains("cpu.resume")) {
			ppssppClient.send(msg.toString());
		}
		
	}
	
	/**
	 * Method called when websocket receives a response from the server.
	 * @param text - response
	 */
	public void onMessage(String text) {
		JSONObject msg = new JSONObject(text);

		//Check if it is a breakpoint first
		if (msg.getString("event").contains("log") && msg.has("header") && msg.getString("header").contains("Breakpoints")) {
			String log = msg.getString("message");
			String bpName = log.substring(log.indexOf(':') + 1).trim();
			
			Breakpoint bp = breakpoints.get(bpName);
			if (bp == null) {
				return;
			}
			
			ScriptHandler.executeOnTheHunt(bp.getScriptToRun());
			Logger.log(Level.SUCCESS, "Breakpoint[" + bpName  + "] triggered... running script " + bp.getScriptToRun() + ".");
			return;
		}
		
		
		if (!msg.has("ticket")) {
			return;
		}
		Iterator<Request> it = requests.iterator();
		while (it.hasNext()) {
			Request req = it.next();
			if (msg.getLong("ticket") != req.getTicketNum() ) {
				continue;
			}


			if (req.getEvent().equals(msg.get("event")) || msg.getString("event").contains(req.getEvent())) {
				req.completeRequest(text);
				Logger.log(Level.SUCCESS, "Request Complete [Time spent: " + req.getTotalTime() + "ms](ticketNum: " + req.getTicketNum() + ")");
				it.remove();
			}
		}

		/*
		for (int i = 0; i < requests.size(); i++) {
			Request req = requests.get(i);
			
			if (msg.getLong("ticket") != req.getTicketNum() ) {
				continue;
			}
			
			
			if (req.getEvent().equals(msg.get("event")) || msg.getString("event").contains(req.getEvent())) {
				req.completeRequest(text);
				Logger.log(Level.SUCCESS, "Request Complete [Time spent: " + req.getTotalTime() + "ms](ticketNum: " + req.getTicketNum() + ")");
				requests.remove(i);
			}
		}
		*/

	}

	
	/**
	 * Get the next ticket number.
	 * @return
	 */
	private long nextTicketNum() {
		return currentTicketNum++;
	}
	
	
	/**
	 * Sends binary data to the connected webSocket server.
	 * @param data The byte-Array of data to send to the WebSocket server.
	 */
	public void send(byte[] data) {
		ppssppClient.send(data);
	}
	
	
	/**
	 * Checks if the websocket is connected.
	 * @return true if closed, false if connected.
	 */
	public boolean isClosed() {
		if (ppssppClient.isClosed()) {
			Logger.log(Level.ERROR, "WebSocket is disconnected. Attempting to reconnect...");
			if (!reconnect()) {
				return true;
			}
			return false;
		}
		return false;
	}
	
	/**
	 * Automatically obatain the ip and port combo for the running debugger on the network;
	 * Utilizes the existing site made by Unknown. If the site ever goes down the manual
	 * mode is still available.
	 */
	public void autoConnect() {
		try {
			// create and read the line from the site into a String variable. 
			URL report = new URL("https://report.ppsspp.org/match/list");
			
			BufferedReader in = new BufferedReader(new InputStreamReader(report.openStream()));
			
			// removes the open and closing braket which may interfere with the json parser
			String line = in.readLine();
			line = line.replace("[", "").replace("]", "");
			
			// parses the json object to be fed into a new URI.
			JSONObject obj = new JSONObject(line);
			
			serverUri = new URI("ws://" + obj.getString("ip") + ":" + obj.getInt("p") + "/debugger");
			
			// create a websocket client pointing to the new uri.
			ppssppClient = new PPSSPPClient(serverUri);
			connect();
			
			// close inputstream.
			in.close();
		} catch (IOException | JSONException | URISyntaxException e) {
			Logger.log(Level.WARN, "Failed to automatically connect.");
			Logger.log(Level.WARN, "Make sure the debugger is running on the connected network or"
					+ " try to manually connect by setting the port in the websocket.yaml config.");
		}
	}
	
	/**
	 * To be used after a websocket loses connection. Creates a new client and attempts a reconnect. 
	 */
	public boolean reconnect() {
		if (attemptingReconnect) {
			Logger.log(Level.INFO, "A reconnect attempt is in process.");
			return false;
		}
		Logger.log(Level.INFO, "Attempting to reconnect...");
		
		// Confirm there is already an attempt at a reconnect to avoid creating multiple client instances
		attemptingReconnect = true;
		
		ppssppClient = new PPSSPPClient(serverUri);
		
		boolean result = false;
		try {
			result = ppssppClient.connectBlocking(configuration.getWebSocketReconnectTimeout(), TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (!result) {
			Logger.log(Level.WARN, "WebSocket failed to reconnect before " + WebSocketHandler.configuration.getWebSocketReconnectTimeout() + " seconds.");
		}
		
		attemptingReconnect = false;
		return result;
	}
	
	/**
     * Load the Configuration
     */
    public static void loadConfiguration() {
    	
    	File webSocketConfig = new File(Launcher.configPath + File.separator + "websocket.yaml");
    	
    	try {
    		
    		if (!webSocketConfig.exists()) {
        		generateConfig();
        	}
        	
            InputStream is = new BufferedInputStream(new FileInputStream(webSocketConfig));
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            configuration = mapper.readValue(is, WebSocketConfiguration.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.log(Level.FATAL, "Unable to load Configuration ... Exiting.");
            System.exit(1);
        }
    }
    
    
    /**
     * Generates config file.
     */
    public static void generateConfig() {
    	
        try {
        	Logger.log(Level.WARN, "Missing websocket.yaml. Generating new config...");
        	ClassLoader classloader = WebSocketHandler.class.getClassLoader();
        	
        	// copies websocket.yaml template to current working directory.
            InputStream original = classloader.getResourceAsStream("websocket.yaml");
            Path copy = Paths.get(new File(Launcher.configPath + File.separator + "websocket.yaml").toURI());
          
            Logger.log(Level.WARN, "Generating config... Source:" + original + ", Target:" + copy);
			Files.copy(original, copy);
			
		} catch (IOException e) {
			e.printStackTrace();
			Logger.log(Level.DEBUG, "Failed to generate websocket.yaml...");
		}
    }
}
