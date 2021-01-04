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
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import ppssppftpbot.pccb.net.Launcher;
import ppssppftpbot.pccb.net.Logger;
import ppssppftpbot.pccb.net.Logger.Level;

public class WebSocketHandler {

	
	public static WebSocketConfiguration configuration;
	
	public static WebSocketClient ppssppClient;
	
	/**
	 * Used to prevent reconnect spam. Only allow 1 reconnect attempt at a time. 
	 */
	public static boolean attemptingReconnect = false;
	
	public static URI serverUri;
	
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
	 * Sends a text to the connected websocket server
	 * @param text the string which will be transmitted
	 */
	public void send(String text) {
		if (ppssppClient.isClosed()) {
			Logger.log(Level.ERROR, "WebSocket is disconnected. Attempting to reconnect...");
			
			if (!reconnect()) {
				return;
			}
		}
			
		ppssppClient.send(text);
		 Logger.log(Level.INFO, text);
	}
	
	/**
	 * Sends binary data to the connected webSocket server.
	 * @param data The byte-Array of data to send to the WebSocket server.
	 */
	public void send(byte[] data) {
		ppssppClient.send(data);
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
