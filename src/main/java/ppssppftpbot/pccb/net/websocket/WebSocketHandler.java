package ppssppftpbot.pccb.net.websocket;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class WebSocketHandler {

	
	public static WebSocketConfiguration configuration;
	
	public static WebSocketClient ppssppClient;
	
	public static boolean attemptingReconnect = false;
	
	public WebSocketHandler() {
		// Load Configuration
		loadConfiguration();
		System.out.println(configuration.toString());
		// Creates a new WebSocketClient from the PPSSPPClient
		ppssppClient = new PPSSPPClient(configuration.getServerUri());
	}
	
	
	/**
	 * Initializes connection with WebSocket Server
	 */
	public void connect() {
		// Connects to the WebSocket server
		ppssppClient.connect();
	}
	
	/**
	 * Initiates the websocket close handshake
	 */
	public void close() {
		ppssppClient.close();
	}
	
	
	/**
	 * To be used after a websocket loses connection. Creates a new client and attempts a reconnect. 
	 */
	public boolean reconnect() {
		if (attemptingReconnect) {
			System.out.println("A reconnect attempt is in process.");
			return false;
		}
		
		System.out.println("Attempting to reconnect...");
		
		// Confirm there is already an attempt at a reconnect to avoid creating multiple client instances
		attemptingReconnect = true;
		
		ppssppClient = new PPSSPPClient(configuration.getServerUri());
		
		boolean result = false;
		try {
			result = ppssppClient.connectBlocking(configuration.getWebSocketReconnectTimeout(), TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if (!result) {
			System.out.println("WebSocket failed to reconnect before " + WebSocketHandler.configuration.getWebSocketReconnectTimeout() + " seconds.");
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
			System.out.println("WebSocket is disconnected. Attempting to reconnect...");
			
			if (!reconnect()) {
				return;
			}
		}
			
		ppssppClient.send(text);
		System.out.println(text);
	}
	
	/**
	 * Sends binary data to the connected webSocket server.
	 * @param data The byte-Array of data to send to the WebSocket server.
	 */
	public void send(byte[] data) {
		ppssppClient.send(data);
		
	}
	
	/**
     * Load the Configuration
     */
    public static void loadConfiguration() {
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream is = classloader.getResourceAsStream("websocket.yaml");

            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            configuration = mapper.readValue(is, WebSocketConfiguration.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Unable to load Configuration ... Exiting.");
            System.exit(1);
        }
    }
}
